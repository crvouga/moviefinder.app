(ns linkpage.backend.request-handler)

(defmulti request-handler!
  "A multimethod that putes on the request URL."
  (fn [req _res] (.-url req)))