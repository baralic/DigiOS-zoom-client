package us.zoom.sdksample.initsdk;

public interface AuthConstants {

	// TODO Change it to your web domain
	public final static String WEB_DOMAIN = "zoom.us";

	/**
	 * We recommend that, you can generate jwttoken on your own server instead of hardcore in the code.
	 * We hardcore it here, just to run the demo.
	 *
	 * You can generate a jwttoken on the https://jwt.io/
	 * with this payload:
	 * {
	 *
	 *     "appKey": "string", // app key
	 *     "iat": long, // access token issue timestamp
	 *     "exp": long, // access token expire time
	 *     "tokenExp": long // token expire time
	 * }
	 */
	public final static String SDK_JWTTOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzZGtLZXkiOiJNZFlGU0MyaVJNU0Z2alU0bXlmcTN3IiwiaWF0IjoxNzAwMjM0MzI1LCJleHAiOjE3MDAyNDE1MjUsImFwcEtleSI6Ik1kWUZTQzJpUk1TRnZqVTRteWZxM3ciLCJ0b2tlbkV4cCI6MTcwMDI0MTUyNX0.Q_e1ZOzSN_ZxTpoudovl-krwwud3bauLWdPvyoNNF2o";
}
