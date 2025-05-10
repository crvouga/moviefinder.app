(ns lib.ui.swiper
  (:require
   [clojure.core.async :as a :refer [chan]]
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



(defn- js-event->slide-index [e]
  (let [detail (.-detail e)
        first-item (aget detail 0)
        active-index (aget first-item "activeIndex")]
    (js/parseInt active-index)))

(defn- js-event->event [e]
  {:swiper/active-index (js-event->slide-index e)})

(defn slide-changed-chan [css-selector]
  (let [c (chan 1 (map js-event->event))]
    (dom/put-events! c css-selector "swiperslidechange")
    c))

