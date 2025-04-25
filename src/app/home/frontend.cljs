(ns app.home.frontend
  (:require
   [app.frontend.config :refer [config]]
   [app.frontend.db :as db]
   [app.frontend.screen :as screen]
   [app.frontend.ui.top-level-bottom-buttons :as top-level-bottom-buttons]
   [app.media.media-db.frontend]
   [app.media.media-db.interface :as media-db]
   [clojure.core.async :as a]
   [core.dom :as dom]
   [core.program :as p]
   [core.ui.image :as image]
   [core.ui.image-preload :as image-preload]
   [core.ui.top-bar :as top-bar]))

(def popular-media-query
  {:query/limit 25
   :query/offset 0
   :query/select [:media/id :media/title :media/year :media/popularity :media/genre-ids :media/poster-url]
   :query/order [:media/popularity :desc]
   :query/where [:query/and
                 [:> :media/popularity 80]
                 [:= :media/media-type :media-type/movie]]})

(a/go-loop []
  (let [query (merge config popular-media-query)
        query-result (a/<! (media-db/query-result-chan! query))]
    (p/put! [:db/got-query-result query-result])
    (a/<! (a/timeout (* 1000 60)))
    (recur)))


(a/go-loop []
  (let [msg (a/<! (p/take! ::clicked-swiper-slide))
        screen-payload (-> msg second (select-keys [:media/id]))
        screen [:screen/media-details screen-payload]
        _ (p/put! [:screen/clicked-link screen])]
    (recur)))

(defn- view-swiper-slide [row]
  [:swiper-slide {}
   [:button.w-full.h-full.overflow-hidden.cursor-pointer.select-none
    {:on-click #(p/put! [::clicked-swiper-slide row])}
    [image-preload/view {:image/url (:media/backdrop-url row)}]
    [image/view {:image/url (:media/poster-url row)
                 :image/alt (:media/title row)
                 :class "pointer-events-none w-full h-full"}]]])

(a/go
  (a/<! (a/timeout 1000))
  (let [swiper-container (dom/query! "#swiper-container")
        swiper-container-event-chan! (dom/event-chan swiper-container "swiperslidechange")]
    (a/go-loop []
      (let [event (a/<! swiper-container-event-chan!)
            slide-index-new (-> event .-detail (aget 0) .-activeIndex js/parseInt)]
        (p/put! [::swiper-slide-changed slide-index-new])
        (recur)))))


(defn- view-swiper [rows]
  [:swiper-container {:class "w-full flex-1 overflow-hidden"
                      :direction :vertical
                      :id "swiper-container"}
   (for [row rows]
     ^{:key row}
     [view-swiper-slide row])])

(screen/register!
 :screen/home
 (fn [input]
   (let [state input
         query-result (db/to-query-result state popular-media-query)
         rows (:query-result/rows query-result)]
     [:div.w-full.flex-1.flex.flex-col.overflow-hidden
      [top-bar/view {:top-bar/title "Home"}]
      [view-swiper rows]
      (when (empty? rows)
        [image/view {:class "w-full h-full"}])
      [top-level-bottom-buttons/view input]])))
