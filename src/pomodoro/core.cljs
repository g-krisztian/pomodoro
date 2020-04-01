(ns pomodoro.core
  (:require [reagent.core :as r]
            [reagent.dom :as rd]
            [reagent.cookies :as rc]
            [pomodoro.audio :as audio]
            [pomodoro.time-format :as tf]
            [cognitect.transit :as t]))

(enable-console-print!)

(defonce app-state (r/atom {:lenght    5
                            :task-name "Default"
                            :now       (.getTime (js/Date.))
                            :key       0}))
(defn get-last-key []
   (-> rc/get
       :history
       (map :key)
       max) )

(defn reset-task []
  (swap! app-state merge {:paused  true
                          :active  false
                          :stop    true
                          :resume  true
                          :elapsed 0
                          :key (get-last-key)}))

(defn update-stop-time []
  (let [now (.getTime (js/Date.))
        start (:start-time @app-state)
        real-duration (- now start)
        paused-duration (- (:paused-time @app-state) start)]
    (if (:paused @app-state false) paused-duration real-duration)))

(defn start-button-on-click [e]
  (swap! app-state merge {:start-time (.getTime (js/Date.))
                          :elapsed    0
                          :paused     false
                          :active     true
                          :stop       false}))

(defn pause-button-on-click []
  (swap! app-state update-in [:paused] not)
  (swap! app-state update-in [:resume] not)
  (swap! app-state merge {:paused-time (.getTime (js/Date.))}))

(defn stop-button-on-click []
  (rc/set! :history
           (conj (rc/get :history)
                 {:task-name (:task-name @app-state)
                  :lenght    (:lenght @app-state)
                  :start     (:start-time @app-state)
                  :key       (:key @app-state)
                  :duration  (update-stop-time)}))
  (reset-task)
  (swap! app-state update-in [:key] inc))

(defn restart-button-on-click [task]
  (swap! app-state merge (select-keys task [:task-name :lenght]))
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
  (swap! app-state merge [key (-> e .-target .-value)]))

(defn start-with-enter [e]
  (when (= 13 (.-charCode e)) (start-button-on-click e)))

(defn text-input [key]
  [:input {:type         "text"
           :value        (key @app-state)
           :on-change    #(swap-value key %)
           :disabled     (:active @app-state)
           :on-key-press start-with-enter}])

(defn number-input [key]
  [:input {:type         "number"
           :value        (key @app-state)
           :on-change    #(swap-value key %)
           :disabled     (:active @app-state)
           :on-key-press start-with-enter}])

(defn finish []
  (stop-button-on-click)
  (audio/playback-mp3))

(defn main-loop []
  (swap! app-state merge [:now (.getTime (js/Date.))])
  (when-not (:paused @app-state)
    (swap! app-state update-in [:elapsed] inc)
    (when (> (:elapsed @app-state) (:lenght @app-state)) (finish))))


(defn progress-bar []
  (let [lenght (:lenght @app-state)
        elapsed (:elapsed @app-state)
        progress (* 100 (/ elapsed lenght))]
    [:div {:class  "progress"
           :margin "0px"
           :style  {:margin "1%"}}
     [:div {:class         "progress-bar"
            :role          "progressbar"
            :style         {:width (str progress "%")}
            :aria-valuemin "0"
            :aria-valuemax 100
            :aria-valuenow progress}
      (tf/render-time (* 1000 elapsed))]]))


(defn summ-usage []
  (->> (rc/get :history)
       (map #(select-keys % [:task-name :duration]))
       (group-by :task-name)
       (map (fn [[k v]] (let [task-name k
                              lenght (reduce + (for [d v] (:duration d)))
                              key (str k lenght)]
                          {:task-name task-name :lenght lenght
                           :key key})))))


(defn change-view [view]
  (swap! app-state merge {:view ""})
  (swap! app-state merge {:view view}))

(defn dropdown-item [view label]
  [:a {:type     "button"
       :class    "dropdown-item"
       :on-click #(change-view view)} label]
  )
(defn history-table []
  (when (rc/contains-key? :history)
    [:div#history
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
          [:td (tf/render-time (* 1000 (:lenght task)))]
          [:td (tf/render-time (:duration task))]
          [:td (button-element :active "Restart" #(restart-button-on-click task))]])]]]))

(defn summary []
  (when (rc/contains-key? :history)
    [:div#summary
     [:table {:class "table table-striped table-bordered" :id "summary"}
     [:thead {:class "thead-dark"}
      [:tr
       [:th "Task name"]
       [:th "Spent time"]
       [:th ""]]]
     [:tbody
      (for [task (into [] (summ-usage))]
        [:tr {:key (:key task)}
         [:td (:task-name task)]
         [:td (tf/render-time (:lenght task))]
         [:td (button-element :active "Restart" #(restart-button-on-click (update-in task [:lenght] quot 1000)))]])]]]))

(defn choose-view []
   [:div {:class "dropdown" :style {:margin "1%"}}
    [:input {:type "button"
             :class "btn btn-secondary dropdown-toggle"
             :id "dropdownMenuButton"
           :data-toggle "dropdown"
           :aria-haspopup true
           :aria-expanded= false
           :value "Choose view"}]
    [:div {:class "dropdown-menu"
           :aria-labelledby "dropdownMenuButton"}
     (dropdown-item :summary "Summary")
     (dropdown-item :history "History")]])

(reset-task)

(defonce ticker
         (js/setInterval main-loop 1000))

;(set! js/document.title "1")

(defn applet []
  (set! js/document.title (tf/render-time (* 1000 (:elapsed @app-state))))
  [:div#app {:style {:margin "1%"}}
   [:h1 "Pomodoro app"]
   [:h3 (str "Time: " (tf/render-time (tf/correct-time (:now @app-state))))]
   [:div {:style {:margin "1%"}}
    (text-input :task-name)
    (number-input :lenght)]
   [:div {:class "btn-group" :style {:margin "1%"}}
    (hideable-button-element :active "Start timer" start-button-on-click)
    (hideable-button-element :paused "Pause timer" pause-button-on-click)
    (hideable-button-element :resume "Resume timer" pause-button-on-click)
    (hideable-button-element :stop "Stop timer" stop-button-on-click)]
   (progress-bar)
   [:p   (apply max (map :key (rc/get :history)
         ;map :key
         ;max
         ))]
   (choose-view)
   [:div {:style {:margin "1%"}}
    (condp = (:view @app-state)
      :summary (summary)
      :history (history-table)
      [:p])
      ]])


(rd/render [applet] (. js/document (getElementById "app")))  

