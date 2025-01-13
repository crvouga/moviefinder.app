(ns linkpage.backend.request-handler)

(defmulti request-handler! (fn [req _res] (.-url req)))