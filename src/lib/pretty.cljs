(ns lib.pretty
  (:require
   [clojure.pprint :refer [pprint]]))

(defn str-edn [edn]
  (with-out-str
    (binding [*print-level* nil
              *print-length* nil]
      (pprint edn))))


(defn log [print-level edn]
  (binding [*print-level* print-level]
    (js/console.log (with-out-str (pprint edn)))))