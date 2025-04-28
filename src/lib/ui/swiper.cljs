(ns lib.ui.swiper)

(defn container [props & children]
  (let [default-props {:class "flex-1 w-full h-full"
                       "slides-per-view" "1"
                       :direction "vertical"}]
    [:swiper-container (merge default-props props)
     children]))

(defn slide [props & children]
  [:swiper-slide props children])