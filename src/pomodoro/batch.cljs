(ns pomodoro.batch
  (:require [pomodoro.ui-common :as ui]
            [pomodoro.common :as common]
            [reagent.cookies :as rc]
            [pomodoro.time-format :as tf]
            [pomodoro.action :as action]))

(defn add-to-plan [task]
  (rc/set! :plan (conj (rc/get :plan []) task)))

(defn add-new-task-to-plan []
  (add-to-plan (action/new-task)))

(defn add-to-plan-on-enter [event]
  (when (= 13 (.-charCode event)) (add-new-task-to-plan)))

(defn get-task-in-milisec [task]
  (* 1000 (action/get-task-in-seconds task)))

(defn run-next-item []
  (let [plan (:remain-plan @common/app-state)]
    (action/add-to-history)
    (if (empty? plan)
      (action/reset-task)
      (action/start-plan-on-click plan))))

(defn plan-runner []
  (when (rc/contains-key? :plan)
    [:div
     [:div {:class "btn-group" :style {:margin-top "1%"}}
      (ui/hideable-button-element :active "Start batch" #(action/start-plan-on-click (rc/get :plan [])))
      (ui/hideable-button-element :paused "Pause timer" action/pause-button-on-click)
      (ui/hideable-button-element :resume "Resume timer" action/pause-button-on-click)
      (when-not (empty? (:remain-plan @common/app-state)) (ui/hideable-button-element :stop "Run next" run-next-item))
      (ui/hideable-button-element :stop "Stop batch" action/stop-button-on-click)]

     [:div {:style {:margin-top "1%"}}
      (ui/progress-bar)]]))

(defn plan-table []
  [:div#plan
   (when (rc/contains-key? :plan)
     [:table {:class "table table-striped table-bordered" :id "summary"}
      [:thead {:class "thead-dark"}
       [:tr
        [:th "Task name"]
        [:th (str "Planned time: " (tf/render-time (* 1000 (reduce + (map long (map :length-in-seconds (rc/get :plan)))))))]
        [:th (ui/button-element :active "Clear plan" #(rc/remove! :plan))]]]
      (into [:tbody]
            (for [task (rc/get :plan)]
              [:tr {:key (:key task)}
               [:td (:task-name task)]
               [:td (tf/render-time (get-task-in-milisec task))]
               [:td (ui/button-element :active "Remove" #(rc/set! :plan (remove (fn [t] (= t task)) (rc/get :plan))))]]))])])


(defn planning []
  [:div
   [:h3 "Planning a batch run"]
   (ui/text-input :task-name add-to-plan-on-enter)
   (ui/input-length :length add-to-plan-on-enter)
   (let [long-break {:task-name "Long break" :length-in-seconds 900 :length 15 :unit :min :key (str "plan_" ((@common/app-state :get-key)))}
         short-break {:task-name "Short break" :length-in-seconds 300 :length 5 :unit :min :key (str "plan_" ((@common/app-state :get-key)))}]
     [:div {:class "btn-group"}
      (ui/button-element :active "Add task" #(add-new-task-to-plan))
      (ui/button-element :active "Add short break" #(add-to-plan short-break))
      (ui/button-element :active "Add long break" #(add-to-plan long-break))])
   [:p]
   (plan-table)
   (plan-runner)])