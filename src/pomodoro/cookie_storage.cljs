(ns pomodoro.cookie-storage
  (:require [reagent.cookies]
            [reagent.cookies :as rc]))

(defn get-next-key []
  (rc/get :next-key))

(defn set-next-key [k]
  (rc/set! :next-key k))

(defn get-unit []
  (rc/get :unit))

(defn set-unit [u]
  (rc/set! :unit u))

(defn get-plan []
  (rc/get :plan))

(defn set-plan [p]
  (rc/set! :plan p))

(defn contains-plan? []
  (rc/contains-key? :plan))

(defn delete-plan []
  (rc/remove! :plan))

(defn get-history []
  (rc/get :history))

(defn set-history [h]
  (rc/set! :history h))

(defn delete-history []
  rc/remove! :history)

(defn contains-history? []
  (rc/contains-key? :history))