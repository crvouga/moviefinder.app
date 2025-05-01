(ns lib.pretty
  (:require
   [cljs.pprint :as pprint]))

(defn str-edn [edn]
  (with-out-str
    (binding [*print-level* nil
              *print-length* nil]
      (pprint/pprint edn))))