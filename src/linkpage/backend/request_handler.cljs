(ns linkpage.backend.request-handler)

(defmulti request-handler!
  "A multimethod that dispatches on the request URL."
  (fn [req _res] (.-url req)))