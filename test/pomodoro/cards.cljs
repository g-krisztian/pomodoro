(ns ^:figwheel-hooks pomodoro.cards
  (:require
    [devcards.core :as dc]
    [sablono.core :as sab :include-macros true]
    [pomodoro.core :as pc]
    [pomodoro.single-run :as psr]
    [pomodoro.batch :as bs]
    [pomodoro.history :as ph]
    [pomodoro.summary :as ps]
    [pomodoro.ui-common :as pui]
    [pomodoro.cookie-storage :as pcs]
    [pomodoro.init :as init]
    [reagent.core :as r]
    [ajax.core :refer [GET]]
    [pomodoro.main :as main])

  (:require-macros
    [devcards.core :refer [defcard]]))


(defonce state-atom (r/atom {:get-key           pcs/get-key
                             :width             (.-innerWidth js/window)
                             :length            25
                             :length-in-seconds 25
                             :elapsed           10
                             :paused            true
                             :active            true
                             :task-name         "Default"
                             :now               (.getTime (js/Date.))
                             :start-time        (.getTime (js/Date.))
                             :view              :history
                             :unit              :sec}))

(init/init state-atom :pomodoro-cards "hu-HU")

(defn svg [x]
  [:svg {:xmlns       "http://www.w3.org/2000/svg"
         :xmlnsXlink  "http://www.w3.org/1999/xlink"
         :aria-hidden "true"
         :focusable   "false"
         :width       x
         :height      x
         ;:style {:ms-transform "360deg" :webkit-transform "360deg" :transform "360deg"}
         ;:preserveAspectRatio "xMidYMid meet"
         :viewBox     "0 0 28 28"}
   [:path {:d "M12 8v5h5v-2h-3V8z" :fill "#626262"}]
   [:path {:d "M21.292 8.497a8.957 8.957 0 0 0-1.928-2.862a9.004 9.004 0 0 0-4.55-2.452a9.09 9.09 0 0 0-3.626 0a8.965 8.965 0 0 0-4.552 2.453a9.048 9.048 0 0 0-1.928 2.86A8.963 8.963 0 0 0 4 12l.001.025H2L5 16l3-3.975H6.001L6 12a6.957 6.957 0 0 1 1.195-3.913a7.066 7.066 0 0 1 1.891-1.892a7.034 7.034 0 0 1 2.503-1.054a7.003 7.003 0 0 1 8.269 5.445a7.117 7.117 0 0 1 0 2.824a6.936 6.936 0 0 1-1.054 2.503c-.25.371-.537.72-.854 1.036a7.058 7.058 0 0 1-2.225 1.501a6.98 6.98 0 0 1-1.313.408a7.117 7.117 0 0 1-2.823 0a6.957 6.957 0 0 1-2.501-1.053a7.066 7.066 0 0 1-1.037-.855l-1.414 1.414A8.985 8.985 0 0 0 13 21a9.05 9.05 0 0 0 3.503-.707a9.009 9.009 0 0 0 3.959-3.26A8.968 8.968 0 0 0 22 12a8.928 8.928 0 0 0-.708-3.503z" :fill "#626262"}]
   [:rect {:x "0" :y "0" :width "24" :height "24" :fill "rgba(0, 0, 0, 0)"}]])

(defn svg2 [x]
  [:svg {:xmlns               "http://www.w3.org/2000/svg"
         :xmlnsXlink          "http://www.w3.org/1999/xlink"
         :aria-hidden         "true"
         :focusable           "false"
         :width               (str x "px")
         :height              (str x "px")
         ;:style "-ms-transform: rotate(360deg); -webkit-transform: rotate(360deg); transform: rotate(360deg);"
         :preserveAspectRatio "xMidYMid meet"
         :viewBox             "0 0 24 24"}
   [:path {:d "M2 3h2v18H2zm18 0h2v18h-2zM5 13h2v1h2v-1h2v1h2v-1h4v1h2v-4h-2v1h-4v-1h-2v1H9v-1H7v1H5zm0-9v4h2V7h8v1h2V7h2V5h-2V4h-2v1H7V4zm0 13v3h2v-1h2v1h2v-1h8v-2h-8v-1H9v1H7v-1H5z" :fill "#626262"}]])


#_(defn history_svg [x]
    [:span {:class "input-group-text"
            :style {;:width        label-with
                    :focusable    "false"
                    :paddingLeft  "0.5rem"
                    :paddingRight "0.5rem"}}
     (svg2 x)
     "Task name:"])

(defcard app-state state-atom)

(defonce ticker (js/setInterval #(pc/main-loop state-atom) 1000))

(defcard text-input
         (fn [data _]
           (sab/html (pui/text-input data :task-name #())))
         state-atom)

(defcard number-input
         (fn [data _]
           (sab/html (pui/input-length data :length #())))
         state-atom)

(defcard progress-bar
         (fn [data _]
           (sab/html (pui/progress-bar data)))
         state-atom)

(defcard choose-view
         (fn [data _]
           (sab/html (main/choose-view data)))
         state-atom)

(defcard single-run
         (fn [data _]
           (sab/html (psr/single-run data)))
         state-atom)

(defcard batch-view
         (fn [data _]
           (sab/html (bs/planning data)))
         state-atom)

(defcard card-history
         (fn [data _]
           (sab/html (ph/history-table data)))
         state-atom)

(defcard summary-view
         (fn [data _]
           (sab/html (ps/summary data)))
         state-atom)

(devcards.core/start-devcard-ui!)

