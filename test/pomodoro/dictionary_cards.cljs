(ns ^:figwheel-hooks pomodoro.dictionary-cards
  (:require
    [devcards.core :as dc]
    [sablono.core :as sab :include-macros true])
  (:require-macros
    [devcards.core :refer [defcard]]))

(defcard card (sab/html [:div [:p "empty card"]]))


(dc/start-devcard-ui!)