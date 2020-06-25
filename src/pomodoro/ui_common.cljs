(ns pomodoro.ui-common
  (:require [pomodoro.time-format :as tf]
            [pomodoro.action :as action]
            [pomodoro.dictionary :as dict]))

(defn common-button-style [width value callback]
  {:type     :button
   :class    "btn btn-secondary"
   :value    value
   :style    {:width        width
              :text-align   "center"
              :margin       "auto"
              :paddingLeft  "0.3rem"
              :paddingRight "0.3rem"}
   :on-click callback})

(defn common-button [width value callback]
  [:input (common-button-style width value callback)])

(defn button-element [disabled width value callback]
  [:input (assoc (common-button-style width value callback) :disabled disabled)])

(defn dropdown-item [label action]
  [:a {:type     "button"
       :class    "dropdown-item"
       :on-click action
       :key      label}
   label])

(defn dropdown [disable width value & items]
  [:div {:class "dropdown"}
   [:input {:type          "button"
            :class         "btn btn-secondary dropdown-toggle"
            :data-toggle   "dropdown"
            :aria-haspopup true
            :style         {:width width}
            :aria-expanded false
            :disabled      disable
            :value         value}]
   [:div {:class           "dropdown-menu"
          :aria-labelledby "dropdownMenuButton"}
    items]])

(defn text-input [state key action]
  (let [full-with (min 600 (* (:width @state) 0.94))
        label-with (* 0.3 full-with)
        input-with (- full-with label-with)]
    [:div {:class "input-group-prepend" :style {:width full-with}}
     [:span {:class "input-group-text"
             :style {:width        label-with
                     :paddingLeft  "0.5rem"
                     :paddingRight "0.5rem"}}
      (dict/get-text state key label-with)]
     [:input {:type             "text"
              :class            "form-control"
              :style            {:width input-with}
              :value            (key @state)
              :on-change        #(action/swap-value state key %)
              :disabled         (:active @state)
              :on-key-press     action
              :aria-label       "TaskName"
              :aria-describedby "addon-wrapping"}]]))

(defn input-length [state key action]
  (let [full-with (min 600 (* (:width @state) 0.94))
        label-with (* 0.3 full-with)
        input-with (* 0.4 full-with)]
    [:div {:class "input-group-prepend" :style {:width full-with}}
     [:span {:class "input-group-text"
             :style {:width        label-with
                     :paddingLeft  "0.5rem"
                     :paddingRight "0.5rem"}}
      (dict/get-text state key label-with)]
     [:input {:type         "number"
              :class        "form-control"
              :style        {:width input-with}
              :value        (key @state)
              :on-change    #(action/swap-value state key %)
              :on-key-press action
              :disabled     (:active @state)}]
     [:span {:style {:width label-with}}
      (dropdown (:active @state)
                label-with
                (dict/get-text state (@state :unit) label-with)
                (dropdown-item (dict/get-text state :sec label-with) #(action/swap-unit state :sec))
                (dropdown-item (dict/get-text state :min label-with) #(action/swap-unit state :min)))]]))

(defn progress-bar [state]
  (let [{length  :length-in-seconds
         elapsed :elapsed :or {length 1}} @state
        progress (* 100 (/ elapsed length))]
    [:div {:class "progress" :style {:width     (* (:width @state) 0.94)
                                     :max-width 600}}
     [:div {:class         "progress-bar"
            :role          "progressbar"
            :style         {:width (str progress "%")}
            :aria-valuemin "0"
            :aria-valuemax 100
            :aria-valuenow progress}
      (tf/render-time (* 1000 elapsed))]]))

(defn start-button [state width]
  (common-button
    width
    (dict/get-text state :start-timer width)
    #(action/start-button-on-click state)))

(defn start-plan-button [state width]
  (common-button
    width
    (dict/get-text state :start-batch width)
    #(action/start-plan state)))

(defn pause-button [state width]
  (common-button
    width
    (dict/get-text state :pause-timer width)
    #(action/pause-button-on-click state)))

(defn resume-button [state width]
  (common-button
    width
    (dict/get-text state :resume-timer width)
    #(action/pause-button-on-click state)))

(defn run-next-button [state width]
  (common-button
    width
    (dict/get-text state :run-next width)
    #(action/run-next-item state)))

(defn stop-button [state width]
  (common-button
    width
    (dict/get-text state :stop-timer width)
    #(action/stop-button-on-click state)))

(defn control-buttons [state]
  (let [full-width (min 600 (* (:width @state) 0.94))
        width (if (:active @state)
                (* full-width 0.5)
                full-width)]
    [:div
     [:div {:class "btn-group" :style {:margin-top "1%"}}
      (when-not (@state :active) (start-button state width))
      (when-not (@state :paused) (pause-button state width))
      (when-not (@state :resume) (resume-button state width))
      (when-not (@state :stop) (stop-button state width))]
     [:div {:style {:margin-top "1%"}}
      (progress-bar state)]]))