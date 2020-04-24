(ns pomodoro.ui-common
  (:require [pomodoro.time-format :as tf]
            [reagent.cookies :as rc]
            [pomodoro.common :as common]
            [pomodoro.action :as action]))

(defn common-button-style [value callback]
  {:type     :button
   :class    "btn btn-secondary"
   :value    value
   :style    {:width "150px"}
   :on-click callback})

(defn button-element [key value callback]
  [:input (merge (common-button-style value callback)
                 {:disabled (key @common/app-state)})])

(defn hideable-button-element [key value callback]
  (when-not (key @common/app-state) [:input
                                     (common-button-style value callback)]))
(defn swap-value [key e]
  (swap! common/app-state merge {key (-> e .-target .-value)}))

(defn dropdown-item [label action]
  [:a {:type     "button"
       :class    "dropdown-item"
       :on-click action
       :key      label}
   label])

(defn dropdown [value & args]
  [:div {:class "dropdown"}
   [:input {:type          "button"
            :class         "btn btn-secondary dropdown-toggle"
            :data-toggle   "dropdown"
            :aria-haspopup true
            :aria-expanded false
            :disabled      (:active @common/app-state)
            :value         value}]
   [:div {:class           "dropdown-menu"
          :aria-labelledby "dropdownMenuButton"}

    args]])

(defn swap-unit [m]
  (swap! common/app-state merge {:unit m})
  (rc/set! :unit m))

(defn text-input [key action]
  [:div {:class "input-group-prepend"}
   [:span {:class "input-group-text" :style {:min-width "150px"}} "Task name:"]
   [:input {:type             "text"
            :class            "form-control"
            :value            (key @common/app-state)
            :on-change        #(swap-value key %)
            :disabled         (:active @common/app-state)
            :on-key-press     action
            :aria-label       "TaskName"
            :aria-describedby "addon-wrapping"}]])

(defn input-length [key action]
  [:div {:class "input-group-prepend"}
   [:span {:class "input-group-text" :id "addon-wrapping" :style {:min-width "150px"}} "Task length:"]
   [:input {:type         "number"
            :class        "form-control"
            :value        (key @common/app-state)
            :on-change    #(swap-value key %)
            :on-key-press action
            :disabled     (:active @common/app-state)}]
   [:span (dropdown (common/dictionary (:unit @common/app-state))
                    (dropdown-item "Second" #(swap-unit :sec))
                    (dropdown-item "Minute" #(swap-unit :min)))]])

(defn progress-bar []
  (let [lenght (:length-in-seconds @common/app-state 1)
        elapsed (:elapsed @common/app-state)
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
    (hideable-button-element :active "Start timer" #(action/start-button-on-click {:key (common/get-key)}))
    (hideable-button-element :paused "Pause timer" action/pause-button-on-click)
    (hideable-button-element :resume "Resume timer" action/pause-button-on-click)
    (hideable-button-element :stop "Stop timer" action/stop-button-on-click)]
   [:div {:style {:margin-top "1%"}}
    (progress-bar)]])