(ns core.str
  (:require [clojure.string :as str]))

(defn remove-quotes [s]
  (-> s (str/replace "\"" "") (str/replace "'" "")))