(ns lib.query
  (:require
   [cljs.spec.alpha :as s]))


(s/def :query/where vector?)
(s/def :query/limit number?)
(s/def :query/offset number?)
(s/def :query/select (s/coll-of keyword?))
(s/def :query/order vector?)