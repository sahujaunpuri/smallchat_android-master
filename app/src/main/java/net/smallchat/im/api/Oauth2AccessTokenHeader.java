package net.smallchat.im.api;





/**
 * Encapsulation a http accessToken headers. the order of weiboParameters will not be changed.
 * Otherwise the signature should not be calculated right.

 * @author  GuoXin (guoxin@wqdsoft.com)
 */
public class Oauth2AccessTokenHeader extends HttpHeaderFactory {

    @Override
    public String getWeiboAuthHeader(String method, String url, IMParameters params){
        return "OAuth2 " ;
        
    }
	@Override
	public IMParameters generateSignatureList(IMParameters bundle) {
	    return null;
	}

	@Override
	public String generateSignature(String data) {
		return "";
	}

	@Override
	public void addAdditionalParams(IMParameters des, IMParameters src) {
		// TODO Auto-generated method stub
		
	}

}
