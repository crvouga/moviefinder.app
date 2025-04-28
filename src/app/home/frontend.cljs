(ns app.home.frontend
  (:require
   [app.frontend.ctx :refer [ctx]]
   [app.frontend.db :as db]
   [app.frontend.mod :as mod]
   [app.frontend.screen :as screen]
   [app.frontend.ui.top-level-bottom-buttons :as top-level-bottom-buttons]
   [app.media.media-db.frontend]
   [app.media.media-db.interface :as media-db]
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
  (a/go
    (let [query (merge ctx popular-media-query)
          query-result (a/<! (media-db/query-result-chan! query))]
      (db/put-query-result! i query-result)))

  (a/go-loop []
    (let [[_ slide-index] (a/<! (p/take! i ::swiper-slide-changed))]
      (println "swiper-slide-changed" slide-index)
      (recur)))

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

(defn- view [i]
  (let [query-result (db/to-query-result i popular-media-query)
        rows (:query-result/rows query-result)]
    [screen/view-screen i :screen/home
     [top-bar/view {:top-bar/title "Home"}]
     [view-swiper i rows]
     (when (empty? rows)
       [image/view {:class "w-full h-full"}])
     [top-level-bottom-buttons/view i]]))

(mod/reg {:mod/name :mod/home
          :mod/view-fn view
          :mod/logic-fn logic})