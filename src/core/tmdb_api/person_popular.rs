use crate::core::{dynamic_data::DynamicData, url::query_params::QueryParams};

use super::{person::GetPersonResponse, TmdbApi};

impl TmdbApi {
    /// Fetches the list of popular people from the TMDB API.
    ///
    /// # Arguments
    ///
    /// * `page` - The page number to fetch (optional).
    ///
    /// # Returns
    ///
    /// Returns a `PopularPersonResponse` on success or a `String` error message.
    pub async fn person_popular(self: &TmdbApi, page: usize) -> Result<GetPersonResponse, String> {
        let params = QueryParams::empty().insert("page", page.to_string());

        let req = self.to_get_request("/3/person/popular", params);

        let sent = self.http_client.send(req).await;

        let response = sent.map_err(|err| err.to_string())?;

        let parsed = serde_json::from_str::<GetPersonResponse>(&response.clone().to_body_string())
            .map_err(|err| {
                format!(
                    "Error parsing response: {} {}",
                    err,
                    response.to_body_string()
                )
            })?;

        Ok(parsed)
    }
}
