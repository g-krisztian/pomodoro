(ns ^:figwheel-hooks pomodoro.cards
  (:require
    [devcards.core :as dc :include-macros true]             ; <-- here
    [sablono.core :as sab :include-macros true]
    [pomodoro.core :as pc]
    [pomodoro.single-run :as psr]
    [pomodoro.batch :as bs]
    [pomodoro.history :as ph]
    [pomodoro.summary :as ps]
    [pomodoro.ui-common :as pui]
    [reagent.core :as r])
  (:require-macros
    [devcards.core :refer [defcard]]))

(defonce state-atom (r/atom {:dictionary pc/dictionary
                             :length     25
                             :elapsed    1
                             :task-name  "Default"
                             :now        (.getTime (js/Date.))
                             :view       :history
                             :unit       :sec}))

(defcard app-state state-atom)

(defcard text-input
         (sab/html (pui/text-input state-atom :task-name #())))
(defcard number-input
         (sab/html (pui/input-length state-atom :length #())))

(defcard progress-bar
         (sab/html (pui/progress-bar state-atom)))

(defcard choose-view
         (sab/html [:div (pc/choose-view state-atom)]))

(defcard single-run
         (sab/html (psr/single-run state-atom)))

(defcard batch-view
         (sab/html (bs/planning state-atom)))

(defcard batch-plan
         (sab/html (bs/plan-table state-atom)))

(defcard batch-run
         (sab/html (bs/plan-runner state-atom)))

(defcard card-history
         (sab/html (ph/history-table (atom (merge @state-atom {:view :history})))))

(defcard summary-view
         (sab/html (ps/summary state-atom)))

(defn ^:after-load refresh []
  (devcards.core/start-devcard-ui!))

(refresh)