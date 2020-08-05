package com.playerone.simpleweather.network

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONObject

class VolleyGet(
    val url: String,
    val result: (JSONObject) -> Unit,
    val error: (String) -> Unit
) {

    fun GET(vararg params: Pair<String, Any>) {

        //String que va a contener los parametros
        var parametros = "?"

        params.forEach {
            //concatenar los parametros pasados al url
            parametros += "${it.first}=${it.second}&"
        }

        if (parametros.endsWith("&")) {
            parametros = parametros.dropLast(1)

        }
        makeRequest(parametros)
    }

    private fun makeRequest(params: String) {

        val request = JsonObjectRequest(Request.Method.GET, url + params, null,
            { response ->

                result(response)

            }, { volleyError ->

                error(volleyError.message!!)

            })

        volley.add(request)
    }

    companion object {
        var context: Context? = null
        val volley: RequestQueue by lazy {
            Volley.newRequestQueue(
                context
                    ?: throw NullPointerException("Initialize VolleyGet in application class")
            )
        }

        fun init(context: Context) {
            this.context = context
        }
    }
}