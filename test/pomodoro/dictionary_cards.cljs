(ns ^:figwheel-hooks pomodoro.dictionary-cards
  (:require
    [devcards.core :as dc]
    [clojure.string :as string]
    [sablono.core :as sab :include-macros true])
  (:require-macros
      [devcards.core :refer [defcard]]
      [pomodoro.fileaccess :refer [inline-resource files-in-dir read-all-dictionary]]))

(def dictionaries (cljs.reader/read-string (inline-resource "public/dictionaries.edn")))

(def files (files-in-dir "resources/public"))

(def all-dictionary (read-all-dictionary "resources/public"))

(defcard epmty-card (sab/html [:div [:p "empty card"]]))

(defcard file-resources files)

(defcard dicts dictionaries)

(defcard all-dictionaries
         all-dictionary)

(dc/start-devcard-ui!)