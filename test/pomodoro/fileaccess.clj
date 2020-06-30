(ns pomodoro.fileaccess
  (:require [clojure.java.io :as io]
            [clojure.walk :refer [keywordize-keys]]
            [clojure.tools.reader :as reader]))
(defmacro inline-resource [resource-path]
  (slurp (io/resource resource-path)))

(defn dictionary? [file]
  (re-matches #".*dictionary_[a-z]{2}.edn" (.getName file)))

(defmacro files-in-dir [directory]
  (mapv (fn [x] (.getPath x)) (.listFiles (io/file directory))))

(defn extract-code [filename]
  (second (re-matches #".*dictionary_([a-z]{2}).edn" filename)))

(defn map-reader [f]
  (keywordize-keys {(extract-code (.getName f)) (reader/read-string (slurp f))}))

(defmacro read-all-dictionary [directory]
  (->> directory
       (io/file)
       (.listFiles)
       (remove #(.isDirectory %))
       (remove #(.isHidden %))
       (filter dictionary?)
       (mapv map-reader)))


