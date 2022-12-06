package xyz.yison.server;

import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;

@Component
public class Ping {

    public boolean isConnection(String host){
        try {
            InetAddress geek = InetAddress.getByName(host);
            if (geek.isReachable(5000))
              return true;
            else
              return false;
        } catch (final MalformedURLException e) {
            return false;
        } catch (final IOException e) {
            return false;
        }
    }


    @Retryable(value = Exception.class, maxAttempts = 3, backoff = @Backoff(delay = 5000) )
	public boolean IsConnectionRetry(String host) throws Exception {
    	if(!isConnection(host)){
    		throw new Exception("。。。");
		}
    	return true;
	}

	@Recover
    public boolean recover(Exception e) {
        return false;
    }
}
