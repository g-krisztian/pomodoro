(ns pomodoro.main
  (:require [pomodoro.ui-common :as ui]
            [pomodoro.single-run :as single]
            [pomodoro.batch :as batch]
            [pomodoro.history :as history]
            [pomodoro.summary :as summary]
            [pomodoro.time-format :as tf]
            [pomodoro.dictionary :as dict]))

(defn set-title [state]
  (set! js/document.title (str "Pompdoro - "
                               (dict/get-text state (:view @state))
                               " "
                               (when
                                 (:active @state)
                                 (str "| "
                                      (:task-name @state)
                                      ": "
                                      (tf/render-time (* 1000 (:elapsed @state))))))))

(defn show-view [state]
  (condp = (:view @state)
    :summary (summary/summary state)
    :history (history/history-table state)
    :planning (batch/planning state)
    (single/single-run state)))

(defn swap-view [state view]
  (swap! state merge {:view view}))

(defn choose-view [state]
  (let [views [:single-run :planning :history :summary]
        width (* 0.94 (:width @state))
        button-width (min 150 (* width 0.25))]
    [:div
     (into [:div {:class "btn-group"}]
           (for [view views]
             (ui/button-element
               (@state :active)
               button-width
               (dict/get-text state view button-width)
               #(swap-view state view))))
     [:p]]))

(defn lang-switcher [state]
  [:div#lang-switcher {:style {:float      :right
                               :margin-top "2.8rem"}}
   (let [label (dict/get-text state :language)
         width (max 48 (* 1.1 (dict/measure-text label)))
         dictionaries (:dictionaries @state)]
     (ui/dropdown false width label
          (map
            (fn [d] (ui/dropdown-item (:language d) #(dict/get-dictionary state (:code d))))
            dictionaries)))
   [:p]])

(defn main [state & context]
  (into [:div#:app {:style {:margin "auto"
                            :width  "max-content"}}
         [:div {:style {:float :left}}
          [:h1 (if (> 385 (:width @state))
                 "Pomodoro"
                 "Pomodoro app")]
          [:h3 (str "Time: " (tf/render-time (tf/correct-time (:now @state))))]]]
        context))

