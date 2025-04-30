(ns lib.err)

(defmulti message :err/err)

(defmethod message :default []
  "Something went wrong")