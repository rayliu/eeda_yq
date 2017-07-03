package config;

import java.io.Writer;

import org.beetl.core.ConsoleErrorHandler;
import org.beetl.core.exception.BeetlException;
import org.beetl.core.exception.ErrorInfo;

import com.jfinal.config.JFinalConfig;
import com.jfinal.core.ActionException;

public class ReThrowConsoleErrorHandler extends ConsoleErrorHandler {
    @Override
    public void processExcption(BeetlException ex, Writer writer){
            super.processExcption(ex, writer);
            ex.printStackTrace();
            
            ErrorInfo error = new ErrorInfo(ex);
            if (error.getErrorCode().equals(BeetlException.TEMPLATE_LOAD_ERROR))
            {
                //404
                ActionException e = new ActionException(404, "/WebAdmin/err404.html");
                throw e;
            }
            throw ex;
    }
}
