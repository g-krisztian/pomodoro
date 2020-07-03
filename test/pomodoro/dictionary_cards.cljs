(ns ^:figwheel-hooks pomodoro.dictionary-cards
  (:require
    [devcards.core :as dc]
    [clojure.string :as string]
    [sablono.core :as sab :include-macros true]
    [pomodoro.dictionary :as dict])
  (:require-macros
    [devcards.core :refer [defcard]]
    [pomodoro.fileaccess :refer [inline-resource files-in-dir read-all-dictionary languages]]))

(def dictionaries (cljs.reader/read-string (inline-resource "public/dictionaries.edn")))

(def files (files-in-dir "resources/public"))

(def all-dictionary (read-all-dictionary "resources/public"))

(defcard dicts dictionaries)

(defn block [text]
  [:p {:class "input-group-text"
       :id    "text-meter"
       :style {
               :margin       "auto"
               :width        "max-content"
               :paddingLeft  "0.5rem"
               :paddingRight "0.5rem"}} text])

(defcard en
         (sab/html
           [:table {:class "table table-striped table-bordered"
                    :id    "English dictionary"}
            [:thead {:class "thead-dark"}
             [:tr
              [:th "key"]
              [:th "long"]
              [:th "sort"]]]
            (into [:tbody]
                  (for [[k v] (get-in all-dictionary [:en :long])]
                    [:tr {:key (str k v)}
                     [:td (str k)]
                     [:td v]
                     [:td (get-in all-dictionary [:en :short k])]]))]))
(defcard hu
         (sab/html
           [:table {:class "table table-striped table-bordered"
                    :id    "English dictionary"}
            [:thead {:class "thead-dark"}
             [:tr
              [:th "key"]
              [:th "English long"]
              [:th "long"]
              [:th "sort"]]]
            (into [:tbody]
                  (for [[k v] (get-in all-dictionary [:hu :long])]
                    [:tr {:key (str k v)}
                     [:td (str k)]
                     [:td (get-in all-dictionary [:en :long k])]
                     [:td v]
                     [:td (get-in all-dictionary [:hu :short k])]]))]))

(defcard ru
         (sab/html
           [:table {:class "table table-striped table-bordered"
                    :id    "English dictionary"}
            [:thead {:class "thead-dark"}
             [:tr
              [:th "key"]
              [:th "English long"]
              [:th "long"]
              [:th "sort"]]]
            (into [:tbody]
                  (for [[k v] (get-in all-dictionary [:ru :long])]
                    [:tr {:key (str k v)}
                     [:td (str k)]
                     [:td (get-in all-dictionary [:en :long k])]
                     [:td v]
                     [:td (get-in all-dictionary [:ru :short k])]]))]))
(defcard it
         (sab/html
           [:table {:class "table table-striped table-bordered"
                    :id    "English dictionary"}
            [:thead {:class "thead-dark"}
             [:tr
              [:th "key"]
              [:th "English long"]
              [:th "long"]
              [:th "sort"]]]
            (into [:tbody]
                  (for [[k v] (get-in all-dictionary [:it :long])]
                    [:tr {:key (str k v)}
                     [:td (str k)]
                     [:td (get-in all-dictionary [:en :long k])]
                     [:td v]
                     [:td (get-in all-dictionary [:it :short k])]]))]))
(defcard tr
         (sab/html
           [:table {:class "table table-striped table-bordered"
                    :id    "English dictionary"}
            [:thead {:class "thead-dark"}
             [:tr
              [:th "key"]
              [:th "English long"]
              [:th "long"]
              [:th "sort"]]]
            (into [:tbody]
                  (for [[k v] (get-in all-dictionary [:tr :long])]
                    [:tr {:key (str k v)}
                     [:td (str k)]
                     [:td (get-in all-dictionary [:en :long k])]
                     [:td v]
                     [:td (get-in all-dictionary [:tr :short k])]]))]))


(dc/start-devcard-ui!)
