(ns lib.ui.cn
  (:require
   [clojure.string :as str]))

(defn cn [& args]
  (->> args
       flatten
       (filter some?)
       (str/join " ")))