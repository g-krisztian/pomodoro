(ns pomodoro.core
  (:require [reagent.core :as r]
            [reagent.dom :as rd]
            [reagent.cookies :as rc]
            [pomodoro.audio :as audio]
            [pomodoro.time-format :as tf]))

(enable-console-print!)

(when-not (or (rc/get :plan) (rc/get :history)) (rc/set! :next-key 0))

(defn get-key []
  (let [actual (rc/get :next-key 0)]
    (rc/set! :next-key (inc actual))
    actual))

(defonce app-state (r/atom {:length    25
                            :elapsed   0
                            :task-name "Default"
                            :now       (.getTime (js/Date.))
                            :view      :single-run
                            :unit      (rc/get :unit :min)}))

(def dictionary {:summary    "Summary"
                 :history    "History"
                 :planning   "Planning"
                 :single-run "Single run"
                 :sec        "Second"
                 :min        "Minute"})

(defn reset-task []
  (swap! app-state merge {:paused  true
                          :active  false
                          :stop    true
                          :resume  true
                          :elapsed 0}))

(defn update-stop-time []
  (let [now (.getTime (js/Date.))
        start (:start-time @app-state)
        real-duration (- now start)
        paused-duration (- (:paused-time @app-state) start)]
    (if (:paused @app-state false) paused-duration real-duration)))

(defn length-to-seconds [length]
  (if (= :min (:unit @app-state)) (* 60 length) length))

(defn start-button-on-click [e]
  (swap! app-state merge {:start-time        (.getTime (js/Date.))
                          :elapsed           0
                          :paused            false
                          :active            true
                          :stop              false
                          :length-in-seconds (length-to-seconds (:length @app-state))}))

(defn pause-button-on-click []
  (swap! app-state update-in [:paused] not)
  (swap! app-state update-in [:resume] not)
  (swap! app-state merge {:paused-time (.getTime (js/Date.))}))

(defn stop-button-on-click []
  (rc/set! :history
           (conj (rc/get :history)
                 {:task-name (:task-name @app-state)
                  :length    (:length-in-seconds @app-state)
                  :start     (:start-time @app-state)
                  :key       (get-key)
                  :duration  (update-stop-time)}))
  (reset-task))

(defn restart-button-on-click [task]
  (swap! app-state merge (select-keys task [:task-name :length]))
  (start-button-on-click nil))

(defn delete-history-on-click []
  (rc/remove! :history))

(defn common-button-style [value callback]
  {:type     :button
   :value    value
   :style    {:width "150px"}
   :on-click callback})

(defn hideable-button-element [key value callback]
  [:input
   (merge
     (common-button-style value callback)
     {:style (merge {:width "150px"} (when (key @app-state) {:display "none"}))})])

(defn button-element [key value callback]
  [:input (merge (common-button-style value callback)
                 {:disabled (key @app-state)})])

(defn swap-value [key e]
  (swap! app-state merge {key (-> e .-target .-value)}))

(defn start-with-enter [e ac]
  (when (= 13 (.-charCode e)) (ac e)))

(defn text-input [key enter-action]
  [:div {:classs "input-group"}
   [:div {:class "input-group-prepend"}
    [:span {:class "input-group-text" :style {:min-width "150px"}} "Task name:"]
    [:input {:type             "text"
             :class            "form-control"
             :value            (key @app-state)
             :on-change        #(swap-value key %)
             :disabled         (:active @app-state)
             :on-key-press     enter-action
             :aria-label       "TaskName"
             :aria-describedby "addon-wrapping"}]]
   ])

(defn dropdown-item [label action]
  [:a {:type     "button"
       :class    "dropdown-item"
       :on-click action
       :key      label}
   label]
  )

(defn dropdown [value & args]
  [:div {:class "dropdown"}
   [:input {:type          "button"
            :class         "btn btn-secondary dropdown-toggle"
            :data-toggle   "dropdown"
            :aria-haspopup true
            :aria-expanded false
            :disabled      (:active @app-state)
            :value         value}]
   [:div {:class           "dropdown-menu"
          :aria-labelledby "dropdownMenuButton"
          }
    args]])

(defn swap-unit [m]
  (swap! app-state merge {:unit m})
  (rc/set! :unit m))

(defn input-length [key enter-action]
  [:div {:class "input-group-prepend"}
   [:span {:class "input-group-text" :id "addon-wrapping" :style {:min-width "150px"}} "Task length:"]
   [:input {:type         "number"
            :class        "form-control"
            :value        (key @app-state)
            :on-change    #(swap-value key %)
            :on-key-press enter-action
            :disabled     (:active @app-state)}]
   [:span (dropdown (dictionary (:unit @app-state))
                    (dropdown-item "Second" #(swap-unit :sec))
                    (dropdown-item "Minute" #(swap-unit :min)))]])

(defn finish []
  (stop-button-on-click)
  (audio/playback-mp3))

(defn main-loop []
  (swap! app-state merge [:now (.getTime (js/Date.))])
  (when-not (:paused @app-state)
    (swap! app-state update-in [:elapsed] inc)
    (when (> (:elapsed @app-state) (:length-in-seconds @app-state)) (finish))))

(defn summ-usage []
  (->> (rc/get :history)
       (map #(select-keys % [:task-name :duration]))
       (group-by :task-name)
       (map (fn [[task-name v]] (let [length (reduce + (for [d v] (:duration d)))
                                      key task-name]
                                  {:task-name task-name
                                   :length    length
                                   :key       key})))))

(defn summary []
  [:div#summary
   [:h3 "Summary"]
   (when (rc/contains-key? :history)
     [:table {:class "table table-striped table-bordered" :id "summary"}
      [:thead {:class "thead-dark"}
       [:tr
        [:th "Task name"]
        [:th "Spent time"]
        [:th ""]]]
      [:tbody
       (for [task (into [] (summ-usage))]
         [:tr {:key task}
          [:td (:task-name task)]
          [:td (tf/render-time (:length task))]
          [:td (button-element :active "Restart" #(restart-button-on-click (update-in task [:length] quot 1000)))]])]])])

(defn history-table []
  [:div#history
   [:h3 "History"]
   (when (rc/contains-key? :history)
     [:table {:class "table table-striped table-bordered" :id "history"}
      [:thead {:class "thead-dark"}
       [:tr
        [:th "Task name"]
        [:th "Start time"]
        [:th "Planned duration"]
        [:th "Real duration"]
        [:th (button-element :active "Delete history" delete-history-on-click)]]]
      [:tbody
       (for [task (rc/get :history)]
         [:tr {:key (:key task)}
          [:td (:task-name task)]
          [:td (tf/render-time (tf/correct-time (:start task)))]
          [:td (tf/render-time (* 1000 (:length task)))]
          [:td (tf/render-time (:duration task))]
          [:td (button-element :active "Restart" #(restart-button-on-click task))]])]])])

(defn progress-bar []
  (let [lenght (:length-in-seconds @app-state 1)
        elapsed (:elapsed @app-state)
        progress (* 100 (/ elapsed lenght))]
    [:div {:class "progress"}
     [:div {:class         "progress-bar"
            :role          "progressbar"
            :style         {:width (str progress "%")}
            :aria-valuemin "0"
            :aria-valuemax 100
            :aria-valuenow progress}
      (tf/render-time (* 1000 elapsed))]]))

(defn control-buttons []
  [:div
   [:div {:class "btn-group" :style {:margin-top "1%"}}
    (hideable-button-element :active "Start timer" start-button-on-click)
    (hideable-button-element :paused "Pause timer" pause-button-on-click)
    (hideable-button-element :resume "Resume timer" pause-button-on-click)
    (hideable-button-element :stop "Stop timer" stop-button-on-click)
    ]
   [:div {:style {:margin-top "1%"}}
    (progress-bar)]])

(defn single-run []
  [:div#single-run
   [:div
    [:h3 "Single run"]
    (text-input :task-name #(start-with-enter % start-button-on-click))
    (input-length :length #(start-with-enter % start-button-on-click))]
   (control-buttons)])

(defn plan-table []
  [:div#planning
   (when (rc/contains-key? :plan)
     [:table {:class "table table-striped table-bordered" :id "summary"}
      [:thead {:class "thead-dark"}
       [:tr
        [:th "Task name"]
        [:th "Planned time"]
        [:th (button-element :active "Clear plan" #(rc/remove! :plan))]]]
      [:tbody
       (for [task (reverse (rc/get :plan))]
         [:tr {:key (:key task)}
          [:td (:task-name task)]
          [:td (tf/render-time (:length task))]
          [:td (button-element :active "Remove" #(restart-button-on-click (update-in task [:length] quot 1000)))]])]])])

(defn add-to-plan [task-name length]
  (rc/set! :plan (cons {:key (get-key) :task-name task-name :length (* 1000 length)} (rc/get :plan))))


(defn start-plan-on-click [plan]
  (println plan)
  (for [task plan]
         (do
           (swap! app-state merge (select-keys task [:key :length :task-name]))
            (start-button-on-click nil))
         )
  )


(defn plan-runner []
  [:div
   [:div {:class "btn-group" :style {:margin-top "1%"}}
    (hideable-button-element :active "Start plan" #(start-plan-on-click (reverse (rc/get :plan))))
    (hideable-button-element :paused "Pause timer" pause-button-on-click)
    (hideable-button-element :resume "Resume timer" pause-button-on-click)
    (hideable-button-element :stop "Stop plan" stop-button-on-click)
    ]
   [:div {:style {:margin-top "1%"}}
    (progress-bar)]]
  )

(defn planning []
  [:div
   [:h3 "Planning a batch run"]
   (text-input :task-name #(start-with-enter % add-to-plan))
   (input-length :length #(start-with-enter % add-to-plan))
   (button-element :add "Add task" #(add-to-plan (:task-name @app-state) (length-to-seconds (:length @app-state))))
   (button-element :add-break "Add short break" #(add-to-plan "Short break" 300))
   (button-element :add-break "Add long break" #(add-to-plan "Long break" 900))
   [:p]
   (plan-table)
   (plan-runner)])

(defn swap-view [view]
  (swap! app-state merge {:view view}))

(defn choose-view [label]
  (let [views [:single-run :summary :history :planning]]
    [:div
     (dropdown (dictionary label)
               (for [view views]
                 (dropdown-item (dictionary view) #(swap-view view))))
     [:p]
     (condp = label
       :summary (summary)
       :history (history-table)
       :planning (planning)
       (single-run))]))

(reset-task)

(defonce ticker
         (js/setInterval main-loop 1000))

(defn set-title []
  (set! js/document.title (str "Pompdoro - "
                               ((:view @app-state) dictionary)
                               " "
                               (when
                                 (:active @app-state)
                                 (str "| "
                                   (:task-name @app-state)
                                   ": "
                                   (tf/render-time (* 1000 (:elapsed @app-state))))))))

(defn applet []
  (set-title)
  [:div#app {:style {:margin "1%"}}
   [:h1 "Pomodoro app"]
   [:h3 (str "Time: " (tf/render-time (tf/correct-time (:now @app-state))))]
   [:p (str @app-state)]
   [:p (str (rc/get :plan))]
   (choose-view (:view @app-state))])

(rd/render [applet] (. js/document (getElementById "app")))

