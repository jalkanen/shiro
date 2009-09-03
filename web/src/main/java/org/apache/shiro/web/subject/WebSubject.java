package org.apache.shiro.web.subject;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.mgt.SubjectFactory;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.WebUtils;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * A {@code WebSubject} represents a Subject instance that was acquired as a result of an incoming
 * {@link ServletRequest}.
 *
 * @since 1.0
 */
public interface WebSubject extends Subject {

    /**
     * Returns the {@code ServletRequest} accessible when the Subject instance was created.
     *
     * @return the {@code ServletRequest} accessible when the Subject instance was created.
     */
    ServletRequest getServletRequest();

    /**
     * Returns the {@code ServletResponse} accessible when the Subject instance was created.
     *
     * @return the {@code ServletResponse} accessible when the Subject instance was created.
     */
    ServletResponse getServletResponse();

    public static class Builder extends Subject.Builder {

        public Builder() {
            this(WebUtils.getRequiredServletRequest(), WebUtils.getRequiredServletResponse());
        }

        public Builder(ServletRequest request, ServletResponse response) {
            this(SecurityUtils.getSecurityManager(), request, response);
        }

        public Builder(SecurityManager securityManager, ServletRequest request, ServletResponse response) {
            super(securityManager);
            if (request == null) {
                throw new IllegalArgumentException("ServletRequest argument cannot be null.");
            }
            if (response == null) {
                throw new IllegalArgumentException("ServletResponse argument cannot be null.");
            }
            setRequest(request);
            setResponse(response);
        }

        protected Builder setRequest(ServletRequest request) {
            if (request != null) {
                getSubjectContext().put(SubjectFactory.SERVLET_REQUEST, request);
            }
            return this;
        }

        protected Builder setResponse(ServletResponse response) {
            if (response != null) {
                getSubjectContext().put(SubjectFactory.SERVLET_RESPONSE, response);
            }
            return this;
        }

        public WebSubject buildWebSubject() {
            Subject subject = super.buildSubject();
            if (!(subject instanceof WebSubject)) {
                String msg = "Subject implementation returned from the SecurityManager was not a " +
                        WebSubject.class.getName() + " implementation.  Please ensure a Web-enabled SecurityManager " +
                        "has been configured and made available to this builder.";
                throw new IllegalStateException(msg);
            }
            return (WebSubject) subject;
        }
    }

}
