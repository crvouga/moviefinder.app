(ns app.feed.frontend
  (:require
   [app.feed.edit.frontend]
   [app.feed.feed :as feed]
   [app.feed.feed-db.impl]
   [app.feed.feed-db.inter :as feed-db]
   [app.frontend.db :as db]
   [app.frontend.mod :as mod]
   [app.frontend.screen :as screen]
   [app.frontend.ui.top-level-bottom-buttons :as top-level-bottom-buttons]
   [app.media.details.frontend :as media-details]
   [app.media.media-db.frontend]
   [app.media.media-db.inter :as media-db]
   [clojure.core.async :refer [<! go go-loop]]
   [lib.program :as p]
   [lib.ui.bar :as bar]
   [lib.ui.icon :as icon]
   [lib.ui.icon-button :as icon-button]
   [lib.ui.image :as image]
   [lib.ui.image-preload :as image-preload]
   [lib.ui.swiper :as swiper]))


#_(def swiper-slide-index-chan
    (swiper/slide-index-chan "#swiper-container"))

(defn load [i]
  (go
    (let [media-query-result (<! (media-db/query! i (feed/to-media-query {})))]
      (p/put! i [:db/got-query-result media-query-result]))))

(defn- logic [i]
  (screen/take-every! i :screen/feed (fn [_] (p/put! i [::load])))
  (p/take-every! i ::load (fn [_] (load i)))
  (p/take-every! i ::swiper-slide-changed (fn [[_ idx]] (println "swiper-slide-changed" idx)))
  #_(go-loop []
      (let [slide-index (<! swiper-slide-index-chan)]
        (p/put! i [::swiper-slide-changed slide-index])
        (recur))))

;; 
;; 
;; 
;; 

(defn- view-swiper-slide [i row]
  [swiper/slide {}
   [:button.w-full.h-full.overflow-hidden.cursor-pointer.select-none
    {:on-click #(p/put! i [:screen/clicked-link (media-details/to-screen row)])}
    [image-preload/view {:image/url (:media/backdrop-url row)}]
    [image/view {:image/url (:media/poster-url row)
                 :image/alt (:media/title row)
                 :class "w-full h-full"}]]])


(defn- view-swiper-last-slide [_]
  [swiper/slide {}
   [image/view {:class "w-full h-full"}]])

(defn- view-swiper [i rows]
  [swiper/container
   {:class "w-full flex-1 overflow-hidden"
    :direction :vertical
    :id "swiper-container"}
   (for [row rows]
     ^{:key row}
     [view-swiper-slide i row])
   (view-swiper-last-slide i)])

(defn view-top-bar [i]
  [:button.w-full.cursor-pointer.select-none
   {:on-pointer-down #(p/put! i [:screen/clicked-link [:screen/feed-edit]])}
   [:div.flex-1.flex.items-center.justify-end.h-full.px-4 {:class bar/h-class}
    [icon-button/view {:icon-button/view-icon icon/adjustments-horizontal}]]])

(defn- view [i]
  (let [query-result (db/to-query-result i (feed/to-media-query {}))
        rows (:query-result/rows query-result)]
    [screen/view-screen i :screen/feed
     [view-top-bar i]
     [view-swiper i rows]
     (when (empty? rows)
       [image/view {:class "w-full h-full"}])
     [top-level-bottom-buttons/view i]]))

(mod/reg {:mod/name ::mod
          :mod/view view
          :mod/logic logic})