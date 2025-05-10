(ns lib.query
  (:require
   [cljs.spec.alpha :as s]
   [clojure.math :as math]))


(s/def :q/where vector?)
(s/def :q/limit number?)
(s/def :q/offset number?)
(s/def :q/select (s/coll-of keyword?))
(s/def :q/order vector?)


(defn to-page-count [q page-size]
  (math/ceil (/ (:q/limit q) page-size)))

(defn to-start-page-index [{:keys [q/offset]} page-size]
  (math/floor (/ offset page-size)))

(defn to-page-index [q page-size]
  (math/floor (mod (:q/offset q) page-size)))

(defn- to-end-page-offset [q page-size]
  (if (zero? (to-page-index q page-size)) 0 1))

(defn to-end-page-index [q page-size]
  (+ (to-start-page-index q page-size)
     (to-page-count q page-size)
     (to-end-page-offset q page-size)))

(comment
  ;; impl From<(Pagination, usize)> for PageBased {
  ;;     fn from((pagination, page_size): (Pagination, usize)) -> Self {
  ;;         let page_count = (pagination.limit as f64 / page_size as f64).ceil() as usize;
  ;;         let start_page = (pagination.offset / page_size) + 1;
  ;;         let index = pagination.offset % page_size;
  ;;         let end_page_offset = if index == 0 { 0 } else { 1 };
  ;;         let end_page = start_page + page_count - 1 + end_page_offset;

  ;;         PageBased {
  ;;             start_page,
  ;;             end_page,
  ;;             page_size,
  ;;             index,
  ;;         }
  ;;     }
  ;; }
  )

(defn to-page-indexes [q page-size]
  (let [start-page-index (to-start-page-index q page-size)
        end-page-index (to-end-page-index q page-size)
        page-indexes (range start-page-index end-page-index)]
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