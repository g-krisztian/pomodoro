(ns pomodoro.ui-common
  (:require [pomodoro.time-format :as tf]
            [reagent.cookies :as rc]
            [pomodoro.action :as action]))

(defn common-button-style [value callback]
  {:type     :button
   :class    "btn btn-secondary"
   :value    value
   :style    {:width "150px"}
   :on-click callback})

(defn button-element [state key value callback]
  [:input (merge (common-button-style value callback)
                 {:disabled (key @state)})])

(defn hideable-button-element [state keyw value callback]
  (when-not (keyw @state) [:input
                           (common-button-style value callback)]))

(defn swap-value [state key e]
  (swap! state merge {key (-> e .-target .-value)}))

(defn dropdown-item [label action]
  [:a {:type     "button"
       :class    "dropdown-item"
       :on-click action
       :key      label}
   label])

(defn dropdown [state value & args]
  [:div {:class "dropdown"}
   [:input {:type          "button"
            :class         "btn btn-secondary dropdown-toggle"
            :data-toggle   "dropdown"
            :aria-haspopup true
            :aria-expanded false
            :disabled      (:active @state)
            :value         value}]
   [:div {:class           "dropdown-menu"
          :aria-labelledby "dropdownMenuButton"}
    args]])

(defn swap-unit [state m]
  (swap! state merge {:unit m})
  (rc/set! :unit m))

(defn text-input [state key action]
  [:div {:class "input-group-prepend"}
   [:span {:class "input-group-text" :style {:min-width "150px"}} "Task name:"]
   [:input {:type             "text"
            :class            "form-control"
            :value            (key @state)
            :on-change        #(swap-value state key %)
            :disabled         (:active @state)
            :on-key-press     action
            :aria-label       "TaskName"
            :aria-describedby "addon-wrapping"}]])

(defn input-length [state key action]
  [:div {:class "input-group-prepend"}
   [:span {:class "input-group-text" :id "addon-wrapping" :style {:min-width "150px"}} "Task length:"]
   [:input {:type         "number"
            :class        "form-control"
            :value        (key @state)
            :on-change    #(swap-value state key %)
            :on-key-press action
            :disabled     (:active @state)}]
   [:span (dropdown state
                    (get-in @state [:dictionary (@state :unit)])
                    (dropdown-item "Second" #(swap-unit state :sec))
                    (dropdown-item "Minute" #(swap-unit state :min)))]])

(defn progress-bar [state]
  (let [length (:length-in-seconds @state 1)
        elapsed (:elapsed @state)
        progress (* 100 (/ elapsed length))]
    [:div {:class "progress"}
     [:div {:class         "progress-bar"
            :role          "progressbar"
            :style         {:width (str progress "%")}
            :aria-valuemin "0"
            :aria-valuemax 100
            :aria-valuenow progress}
      (tf/render-time (* 1000 elapsed))]]))

(defn control-buttons [state]
  [:div
   [:div {:class "btn-group" :style {:margin-top "1%"}}
    (hideable-button-element state :active "Start timer" #(action/start-button-on-click state {:key ((@state :get-key))}))
    (hideable-button-element state :paused "Pause timer" #(action/pause-button-on-click state))
    (hideable-button-element state :resume "Resume timer" #(action/pause-button-on-click state))
    (hideable-button-element state :stop "Stop timer" #(action/stop-button-on-click state))]
   [:div {:style {:margin-top "1%"}}
    (progress-bar state)]])