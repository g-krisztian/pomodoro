(ns pomodoro.batch
  (:require [pomodoro.ui-common :as ui]
            [pomodoro.time-format :as tf]
            [pomodoro.action :as action]
            [pomodoro.dictionary :as dict]
            [pomodoro.cookie-storage :as storage]))

(defn add-to-plan [task]
  (storage/set-plan (conj (or (storage/get-plan) []) task)))

(defn add-new-task-to-plan [state]
  (add-to-plan (action/new-plan state)))

(defn add-to-plan-on-enter [event state]
  (when (= 13 (.-charCode event)) (add-new-task-to-plan state)))

(defn get-task-in-milisec [task]
  (* 1000 (action/get-task-in-seconds task)))

(defn plan-runner [state]
  (when (storage/contains-plan?)
    (let [full-width (min 600 (* (:width @state) 0.94))
          skip-visible (not-empty (:remain-plan @state))
          width (cond
                  (and (:active @state) skip-visible) (* full-width 0.33)
                  (:active @state) (* full-width 0.5)
                  :else full-width)]
      [:div
       [:div {:class "btn-group" :style {:margin-top "1%"}}
        (when-not (@state :active) (ui/start-plan-button state width))
        (when-not (@state :paused) (ui/pause-button state width))
        (when-not (@state :resume) (ui/resume-button state width))
        (when (and (not (@state :stop)) (not-empty (:remain-plan @state)))
          (ui/run-next-button state width))
        (when-not (@state :stop) (ui/stop-button state width))]
       [:div {:style {:margin-top "1%"}}
        (ui/progress-bar state)]])))

(defn plan-table [state plan]
  (let [full-width (* (:width @state) 0.94)
        width (min 120 (* full-width 0.33))]
    [:div#plan
     [:table {
              :class "table table-striped table-bordered" :id "plan"
              :style {:width (min 600 full-width)}}
      [:thead {:class "thead-dark"}
       [:tr
        [:th (dict/get-text state :task-name width)]
        [:th (str (dict/get-text state :planed-time width) (tf/render-time (* 1000 (reduce + (map long (map :length-in-seconds plan))))))]
        [:th (ui/button-element (@state :active) width (dict/get-text state :clear-plan width) #(storage/delete-plan))]]]
      (into [:tbody]
            (for [task plan]
              [:tr {:key (:key task)}
               [:td (:task-name task)]
               [:td (tf/render-time (get-task-in-milisec task))]
               [:td (ui/button-element (@state :active) width (dict/get-text state :remove width) #(storage/set-plan (remove (fn [t] (= t task)) plan)))]]))]]))

(defn short-break [state]
  (add-to-plan {:task-name         (dict/get-text state :short-break)
                :length-in-seconds 300
                :length            5
                :unit              :min
                :key               (str "plan_" ((@state :get-key)))}))

(defn long-break [state]
  (add-to-plan {:task-name         (dict/get-text state :long-break)
                :length-in-seconds 900
                :length            15
                :unit              :min
                :key               (str "plan_" ((@state :get-key)))}))

(defn planning [state]
  (let [width (min 200 (* (:width @state) 0.315))]
    [:div#planning
     [:h3 (dict/get-text state :planning)]
     (ui/text-input state :task-name #(add-to-plan-on-enter % state))
     (ui/input-length state :length #(add-to-plan-on-enter % state))
     [:div {:class "btn-group" :style {:margin-top "1%"}}
      (ui/button-element (@state :active) width (dict/get-text state :add-task width) #(add-new-task-to-plan state))
      (ui/button-element (@state :active) width (dict/get-text state :short-break width) #(short-break state))
      (ui/button-element (@state :active) width (dict/get-text state :long-break width) #(long-break state))]

     (when-let [plan (storage/get-plan)]
       [:div
        [:p]
        (plan-table state plan)
        (plan-runner state)])]))