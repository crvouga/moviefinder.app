(ns lib.query
  (:require
   [cljs.spec.alpha :as s]
   [clojure.math :as math]))


(s/def :q/where vector?)
(s/def :q/limit number?)
(s/def :q/offset number?)
(s/def :q/select (s/coll-of keyword?))
(s/def :q/order vector?)


(defn to-page-count [{:keys [q/limit q/offset]} page-size]
  (quot (+ offset limit) page-size))

(defn to-page-indexes [{:keys [q/limit q/offset]} page-size]
  (let [start-page (math/floor (/ offset page-size))
        end-page (math/ceil (/ (+ offset (max limit 1)) page-size))
        page-indexes (range start-page end-page)]
    (vec page-indexes)))

(defn to-page-numbers [q page-size]
  (let [page-indexes (to-page-indexes q page-size)
        page-numbers (mapv inc page-indexes)]
    page-numbers))

(defn- to-page
  [{:keys [q/limit q/offset]} page-number page-size]
  (let [page-offset (* (dec page-number) page-size)
        drop-amount (max 0 (- offset page-offset))]
    {:page/number page-number
     :page/limit (min limit page-size)
     :page/size page-size
     :page/offset drop-amount}))

(defn to-pages
  [q page-size]
  (let [page-numbers (to-page-numbers q page-size)
        pages (mapv #(to-page q % page-size) page-numbers)]
    pages))