(ns lib.ui.swiper
  (:require
   [lib.dom :as dom]
   [lib.ui.children :as children]))

(defn container [props & children]
  (let [default-props
        {:class "flex-1 w-full h-full"
         :slides-per-view "1"
         :direction :vertical}]
    (children/with
     [:swiper-container (merge default-props props)]
     children)))

(defn slide [props & children]
  (children/with
   [:swiper-slide props]
   children))

(defn- event->slide-index [e]
  (let [detail (.-detail e)
        first-item (aget detail 0)
        active-index (aget first-item "activeIndex")]
    (js/parseInt active-index)))

(defn slide-index-chan [css-selector]
  (dom/watch-event-chan! css-selector "swiperslidechange" (map event->slide-index)))
