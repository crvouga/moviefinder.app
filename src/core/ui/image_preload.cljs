(ns core.ui.image-preload)


(defn view [i]
  (let [img (js/Image.)]
    (set! (.-src img) (:image/url i))
    (fn [] [:<>])))
