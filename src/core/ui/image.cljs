(ns core.ui.image
  (:require [reagent.core :as r]))

(defn- skeleton-surface []
  [:div
   {:class "absolute inset-0 bg-neutral-600 animate-pulse rounded"
    :aria-busy true}])

(defn- is-image-loaded? [url]
  (when url
    (let [img (js/Image.)]
      (= (.-complete img)
         (do (set! (.-src img) url)
             (.-complete img))))))

(defn- image [i loading!]
  [:img {:alt (:image/alt i)
         :src (:image/url i)
         :aria-hidden (not @loading!)
         :style {:outline "none"
                 :border "none"}
         :class (str "w-full h-full object-cover bg-neutral-600 "
                     (when @loading! "opacity-0"))
         :on-load #(reset! loading! false)}])

(defn view [i]
  (let [loading! (r/atom (not (is-image-loaded? (:image/url i))))]
    (fn [i]
      [:div
       {:class (str "relative " (:class i))}
       (when @loading!
         [skeleton-surface])
       [image i loading!]])))