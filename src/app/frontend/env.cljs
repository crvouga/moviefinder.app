(ns app.frontend.env)

(defn dev? []
  (try
    (= (.-hostname js/window.location) "localhost")
    (catch js/Error _ false)))