(ns ^:figwheel-hooks pomodoro.cards
  (:require
    [devcards.core :as dc :include-macros true] ; <-- here
    [sablono.core :as sab :include-macros true]
    [pomodoro.core :as pc]
    [pomodoro.single-run :as psr]
    [pomodoro.batch :as bs]
    [pomodoro.history :as ph]
    [pomodoro.summary :as ps])
  (:require-macros
    [devcards.core :refer [defcard]]))

(defonce state-atom (atom {:dictionary pc/dictionary
                           :length    25
                           :elapsed   0
                           :task-name "Default"
                           :now       (.getTime (js/Date.))
                           :view      :history
                           :unit      :sec}))

(defcard app-state state-atom)

(defcard choose-view
         (sab/html [:div (pc/choose-view state-atom)]))

(defcard single-run
         (sab/html (psr/single-run state-atom)))

(defcard batch-run
         (sab/html (bs/plan-runner state-atom)))

(defcard batch-plan
         (sab/html (bs/plan-table state-atom)))

(defcard card-history
         (sab/html (ph/history-table (atom (merge @state-atom {:view :history})))))


(defn ^:after-load refresh []
  (devcards.core/start-devcard-ui!))

(refresh)