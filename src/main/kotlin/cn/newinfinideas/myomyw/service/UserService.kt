package cn.newinfinideas.myomyw.service

import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import java.time.Instant
import java.util.*


private val KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256) // TODO(construct from a config)
private const val ISSUE = "cn.newinfinideas.myomyw.official" // TODO(read from config)

class User(val name: String) {

}

object UserService {
    fun getAuth(token: String): User? {
        return try {
            val jwt = Jwts.parserBuilder().setSigningKey(KEY).build().parseClaimsJws(token)
            User(jwt.body.subject)
        } catch (e: JwtException) { null }
    }

    fun signIn(username: String): String {
        return Jwts.builder().apply {
            val now = Instant.now()
            setSubject(username)
            setIssuer(ISSUE)
            setIssuedAt(Date(now.toEpochMilli()))
            setNotBefore(Date(now.toEpochMilli()))
        }.signWith(KEY).compact()
    }
}