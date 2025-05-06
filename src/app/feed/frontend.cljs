(ns app.feed.frontend
  (:require
   [app.frontend.db :as db]
   [app.frontend.mod :as mod]
   [app.frontend.screen :as screen]
   [app.frontend.ui.top-level-bottom-buttons :as top-level-bottom-buttons]
   [app.media.media-db.frontend]
   [app.media.media-db.inter :as media-db]
   [clojure.core.async :as a]
   [lib.dom :as dom]
   [lib.program :as p]
   [lib.ui.image :as image]
   [lib.ui.image-preload :as image-preload]
   [lib.ui.top-bar :as top-bar]))

(def popular-media-query
  {:query/limit 25
   :query/offset 0
   :query/select [:media/id :media/title :media/year :media/popularity :media/genre-ids :media/poster-url]
   :query/order [:media/popularity :desc]
   :query/where [:query/and
                 [:> :media/popularity 80]
                 [:= :media/media-type :media-type/movie]]})


;; 
;; 
;; 
;; 
;; 


(defn swiper-event->slide-index [event]
  (let [detail (.-detail event)
        first-item (aget detail 0)
        active-index (aget first-item "activeIndex")]
    (js/parseInt active-index)))

(defn swiper-slide-change-chan []
  (dom/watch-event-chan! "#swiper-container" "swiperslidechange" (map swiper-event->slide-index)))



(defn- logic [i]
  (p/take-every!
   i ::load
   (fn [_]
     (a/go
       (let [query-result (a/<! (media-db/query-result-chan! i popular-media-query))]
         (p/put! i [:db/got-query-result query-result])))))

  (p/take-every!
   i ::swiper-slide-changed
   (fn [[_ slide-index]]
     (println "swiper-slide-changed" slide-index)))

  (p/take-every!
   i :screen/screen-changed
   (fn [[_ [screen-name _]]]
     (when (= screen-name :screen/feed)
       (p/put! i [::load]))))


  (p/take-every!
   i ::clicked-swiper-slide
   (fn [[_ media]]
     (let [screen [:screen/media-details (select-keys media [:media/id])]]
       (p/put! i [:screen/clicked-link screen]))))

  #_(let [slide-index-chan (swiper-slide-change-chan)]
      (a/go-loop []
        (let [slide-index (a/<! slide-index-chan)]
          (p/put! i [::swiper-slide-changed slide-index])
          (recur)))))

;; 
;; 
;; 
;; 

(defn- view-swiper-slide [i row]
  [:swiper-slide {}
   [:button.w-full.h-full.overflow-hidden.cursor-pointer.select-none
    {:on-click #(p/put! i [::clicked-swiper-slide row])}
    [image-preload/view {:image/url (:media/backdrop-url row)}]
    [image/view {:image/url (:media/poster-url row)
                 :image/alt (:media/title row)
                 :class "w-full h-full"}]]])


(defn- view-swiper-last-slide [_]
  [:swiper-slide {}
   [image/view {:class "w-full h-full"}]])

(defn- view-swiper [i rows]
  [:swiper-container {:class "w-full flex-1 overflow-hidden"
                      :direction :vertical
                      :id "swiper-container"}
   (for [row rows]
     ^{:key row}
     [view-swiper-slide i row])
   (view-swiper-last-slide i)])

(defn view-topbar [i]
  [:div.w-full.h-96.border-b])

(defn- view [i]
  (let [query-result (db/to-query-result i popular-media-query)
        rows (:query-result/rows query-result)]
    [screen/view-screen i :screen/feed
     #_[top-bar/view {:top-bar/title "Feed"}]
     [view-swiper i rows]
     (when (empty? rows)
       [image/view {:class "w-full h-full"}])
     [top-level-bottom-buttons/view i]]))

(mod/reg {:mod/name ::mod
          :mod/view view
          :mod/logic logic})