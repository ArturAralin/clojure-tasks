; need to add send body

(require '[clojure.java.io :as io])
(import '[java.net URL])

(def DEFAULT_READ_TIMEOUT 3000)
(def DEFAULT_CONNECTION_TIMEOUT 3000)

(defn set-headers [headers conn]
  (doseq [header headers]
    (let [
          header-name (first header)
          header-val (second header)]
      (.setRequestProperty conn header-name header-val))))

(defn prepare-request-params [opts]
  {
   :url (:url opts)
   :method (:method opts)
   :headers (or (:headers opts) {})
   :connect-timeout (or (:connect-timeout opts) DEFAULT_CONNECTION_TIMEOUT)
   :read-timeout (or (:read-timeout opts) DEFAULT_READ_TIMEOUT)
   })

(defn get-response [conn]
  (apply str
         (let [inp (io/reader (.getInputStream conn))]
           (loop [acc []]
             (let [ch (.read inp)]
               (if-not (= ch -1)
                 (recur (conj acc (char ch)))
                 acc))))))

(defn get-headers [conn]
  (into {}
        (let [headers (.getHeaderFields conn)]
          (map #(vector % (first (map identity (.get headers %))))
               (.keySet headers)))))


(defn http-req [opts]
  (let [
        opts (prepare-request-params opts)
        url (:url opts)
        method (:method opts)
        headers (:headers opts)
        conn (.openConnection (URL. url))]
    ; configure request
    (.setRequestMethod conn method)
    (.setConnectTimeout conn (:connect-timeout opts))
    (.setReadTimeout conn (:read-timeout opts))
    ; set headers
    (set-headers headers conn)
    ; process request
    {
     :request opts
     :response {
                :headers (get-headers conn)
                :body (get-response conn)}
     }))

(http-req {
           :url "http://aaralin.ru"
           :method "GET"})
