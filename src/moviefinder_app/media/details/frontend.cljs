(ns moviefinder-app.media.details.frontend
  (:require
   [moviefinder-app.frontend.screen :as screen]
   [core.ui.top-bar :as top-bar]
   [moviefinder-app.frontend.store :as store]))


(screen/register!
 :screen/media-details
 (fn [i]
   (let [payload (screen/screen-payload i)]
     [:div.w-full.h-full.flex.flex-col
      [top-bar/view {:top-bar/on-back #(store/put! i [:screen/clicked-link [:screen/home]])
                     :top-bar/title (:media/title payload)}]
      [:div (pr-str payload)]])))