app [main] {
    pf: platform "https://github.com/roc-lang/basic-webserver/releases/download/0.5.0/Vq-iXfrRf-aHxhJpAh71uoVUlC-rsWvmjzTYOJKhu4M.tar.br",
    json: "https://github.com/lukewilliamboswell/roc-json/releases/download/0.10.0/KbIfTNbxShRX1A1FgXei1SpO5Jn8sgP6HP6PXbi-xyA.tar.br",
}

import pf.Task
import pf.Http
import pf.Env
import Request
import Response
import Auth.Login
import Hx
import Ctx
import Ctx.Impl
import Route
import Account
import Feed
import App.Document
import Logger
import Media.Details

main : Http.Request -> Task.Task Http.Response []
main = \httpReq ->
    tmdbApiReadAccessToken <- Env.var "TMDB_API_READ_ACCESS_TOKEN" |> Task.onErr (\_ -> Task.ok "") |> Task.await
    databaseUrl <- Env.var "DATABASE_URL" |> Task.onErr (\_ -> Task.ok "") |> Task.await

    req = Request.fromHttp httpReq

    ctx = Ctx.Impl.init { tmdbApiReadAccessToken, databaseUrl } req
    Logger.info! ctx.logger (Inspect.toStr req)

    res =
        if
            Hx.isReq httpReq
        then
            routeHx ctx req
        else
            routeReq req
    res |> Task.map Response.toHttp

routeHx : Ctx.Ctx, Request.Request -> Task.Task Response.Response _
routeHx = \ctx, req ->
    when req.route is
        Login r ->
            Auth.Login.routeHx ctx r

        Feed r ->
            Feed.routeHx ctx r

        Account r ->
            Account.routeHx ctx r

        Media r ->
            Media.Details.routeHx ctx r

        Index | RobotsTxt ->
            Route.init |> Response.redirect |> Task.ok

routeReq : Request.Request -> Task.Task Response.Response _
routeReq = \req ->
    when req.route is
        RobotsTxt ->
            robotsTxt : Str
            robotsTxt =
                """
                User-agent: *
                Allow: /
                """
            Response.text robotsTxt |> Task.ok

        _ ->
            route : Route.Route
            route =
                when req.route is
                    Index -> Route.init
                    _ -> req.route

            App.Document.view { route }
            |> Response.html
            |> Task.ok

