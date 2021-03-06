#ifndef _PROXYHTTPGETSOCKET_H
#define _PROXYHTTPGETSOCKET_H

#include "ServerHandler.h"
#include "HttpGetSocket.h"
#include "Gemensamt.h"
#include <list>

#ifdef SOCKETS_NAMESPACE
namespace SOCKETS_NAMESPACE {
#endif


class ProxyHttpGetSocket : public HttpClientSocket, public Gemensamt
{
public:
	ProxyHttpGetSocket(ISocketHandler& h):HttpClientSocket(h)
	{
		//this->m_httpResponse = "";
	}
	
	ProxyHttpGetSocket(ISocketHandler& h,const std::string& url):HttpClientSocket(h,url)
	{
		
		this->m_cookieSet = false;
		this->m_hostCookieSet = false;
		this->m_cacheControlSet = false;
		this->m_p3pSet = false;
		this->m_transferEndoded = false;
		this->m_varySet = false;
		this->m_contentEncoded = false;
		this->m_redirectLocationSet = false;
		this->m_foundEndHtml = false;
		this->m_proxyHeader = true;
		this->m_foundLen = false;
		this->m_redirectLocationFound = false;
		this->m_continueSet = false;
		this->m_beginContent = false;
		this->m_endContent = false;
		this->m_hadPost = false;
		this->m_httpResponseDataCount = 0;
		this->m_responseHeader = "";
		this->m_destinationIP = "";
		this->m_responseTime = 0.0;

	}

	void SetCookieSet(bool value)
	{
		this->m_cookieSet = value;
	}

	void SetHostCookieSet(bool value)
	{
		this->m_hostCookieSet = value;
	}

	void SetCookieSetValue(std::string value)
	{
		this->m_cookieSetting = value;
	}

	void SetHostCookieSessionValue(std::string value)
	{
		this->m_cookieSession = value;
	}

	void SetRedirectLocation(std::string value)
	{
		this->m_httpRedirectLocation = value;
	}

	void SetResponseTime(double elapsedTime)
	{
		this->m_responseTime = elapsedTime;
	}

	std::string GetHostCookieSessionValue()
	{
		return this->m_cookieSession;
	}

	std::string GetResponseString()
	{
		return this->m_responseData;
	}

	std::string GetCacheControl()
	{
		return this->m_cacheControl;
	}

	std::string GetContentEncoding()
	{
		return this->m_contentEncoding;
	}

	std::string GetContentType()
	{
		return this->m_contentType;
	}

	std::string GetP3pSetting()
	{
		return this->m_p3pSetting;
	}

	std::string GetTransferEncoding()
	{
		return this->m_transferEncoding;
	}

	std::string GetVaryValue()
	{
		return this->m_varyValue;
	}

	std::string GetRedirectLocation()
	{
		return this->m_httpRedirectLocation;
	}

	std::string GetResponseHeader()
	{
		return this->m_responseHeader;
	}

	std::string GetRequestHeader()
	{
		return this->m_requestHeader;
	}

	std::string GetProtocol()
	{
		return "http";
	}

	std::string GetDestinationIP()
	{
		return this->m_destinationIP;
	}

	bool DoneGettingContent()
	{
		return this->m_foundEndHtml;
	}

	bool GzipEncoded()
	{
		return this->m_gzipEncoded;
	}

	bool CacheControlSet()
	{
		return this->m_cacheControlSet;
	}

	bool ContentEncoded()
	{
		return this->m_contentEncoded;
	}

	bool CookieSet()
	{
		return this->m_cookieSet;
	}

	bool HostCookieSet()
	{
		return this->m_hostCookieSet;
	}

	bool P3pSet()
	{
		return this->m_p3pSet;
	}

	bool TransferEncoded()
	{
		return this->m_transferEndoded;
	}

	bool VarySet()
	{
		return this->m_varySet;
	}

	bool RedirectLocationSet()
	{
		return this->m_redirectLocationSet;
	}

	std::string GetResponseStatus()
	{
		return m_responseStatus;
	}

	std::string GetResponseStatusText()
	{
		return m_responseStatusText;
	}

	std::string MyUseragent()
	{
		return GetUserAgent();
	}

	std::string GetCookieSet()
	{
		return this->m_cookieSetting;
	}

	double GetResponseTime()
	{
		return m_responseTime;
	}

	void DoHttpOpperation()
	{
		if(this->m_debugMode)
		{
			printf("in do op\n");
		}

		if (!Open(GetUrlHost(),GetUrlPort()))
		{
			if (!Connecting())
			{
				Handler().LogError(this, "ProxyHttpGetSocket", -1, "connect() failed miserably", LOG_LEVEL_FATAL);
				
				//if(this->m_debugMode)
				{
					printf("connect() failed");
				}

				SetCloseAndDelete();
			}
		}
	}

	void OnRawData(const char *buf,size_t len)
	{
		bool CR = false;
		bool LF = false;
		this->m_foundEndHtml = false;

		for (size_t i = 0; i < len; i++)
		{
			if ((!this->m_proxyHeader)&&(!this->m_continueSet))
			{
				OnData(buf + i,len - i);
				if(this->m_debugMode)
				{
					printf("done getting the data\n");
				}

				break;

			}

			switch (buf[i])
			{
			case 13: // ignore
				CR = true;

				if(this->m_debugMode)
				{
					printf("carriageReturn_");
				}
				break;
			case 10: // end of line
				if(this->m_debugMode)
				{
					printf("lineFeed\n");
				}

				OnLine(m_proxyLine);
				if(CR&&(m_proxyLine.compare("")==0)&&(!this->m_continueSet))
				{
					m_proxyHeader = false;
					CR=false;
					
				}

				if(CR)
				{
					CR=false;
				}

				m_proxyLine = "";
				LF=true;
				break;
			default:
				m_proxyLine += buf[i];
			}
		}
	}
	
	void OnLine(const std::string& line)
	{
		Parse pa(line);
		std::string str = pa.getword();
		if (str.substr(0,4) == "HTTP") // response
		{
			m_responseStatus = pa.getword();
			m_responseStatusText = pa.getrest();
			this->SetStatus(m_responseStatus);
			this->SetStatusText(m_responseStatusText);

			if(this->m_debugMode)
			{
				printf("proxyStatus:%s\n",this->GetStatus().c_str());
				printf("proxyStatusText:%s\n",this->GetStatusText().c_str());
			}

			Parse pa(line,":");
			pa.getword();
			str = pa.getrest();
			//str = this->GetStatusText();
			if(this->m_debugMode)
			{
				printf("proxyStatus string:%s\n",str.c_str());
			}

			m_responseHeader = "";
			
			if(str.compare("302 Found")==0)
			{
				this->m_proxyHeader=true;
				this->m_continueSet = false;
			
				if(this->m_debugMode)
				{
					printf("continue Set!\n");
				}
			}
			else if(str.compare("100 Continue")==0)
			{
				this->m_continueSet = true;
				this->m_proxyHeader=true;
				
				if(this->m_debugMode)
				{
					printf("continue Set!\n");
				}
			}
			else if(str.compare("200 OK")==0)
			{
				this->m_proxyHeader=true;
				this->m_continueSet = false;
				
				if(this->m_debugMode)
				{
					printf("continue unset\n");
				}
			}
			else if(this->m_continueSet)
			{
				this->m_continueSet = false;
				this->m_proxyHeader=true;
			}
			else
			{
				this->m_proxyHeader=true;
			}
		}

		

		if(this->m_proxyHeader)
		{
			Parse pa(line,":");
			std::string key = pa.getword();
			std::string value = pa.getrest();
			
			OnHeader(key,value);

			m_responseHeader+=key.c_str();
			m_responseHeader+=":";
			m_responseHeader+=value.c_str();
			m_responseHeader+="\r\n";
		}

		if((line.compare("")==0)&&(!m_continueSet))
		{
			if(this->m_debugMode)
			{
				printf("end of Header!!!\n");
			}

			m_proxyHeader = false;
			OnHeaderComplete();
			this->m_destinationIP = this->GetRemoteAddress();
		}
	}
	
	void OnData(const char * data, size_t len)
	{
		std::string hexContentLen;
		bool justSetChunkedContentlen = false;
		

		if(this->m_debugMode)
		{
			printf("contentType value:%s\n",this->m_contentType.c_str());
			printf("matching value:text/html\n");
			printf("Starting ContentLen:%d\n",GetHttpContentLength());
			printf("OnData len:%d\n",(int)len);
		}

		//if(strcmp(this->m_contentType.c_str(),"text/html")==0)
		{
			if(!this->m_redirectLocationSet)
			{
				if(this->m_debugMode)
				{
					printf("redirection not set\n"); 
				}

				//int chunkCount=0; 
				std::string tmpData ="";

				for(int i=0;i<(int)len;i++)
				{
					
					//chunkCount = strlen(this->m_responseData.c_str());
	
					tmpData += data[i];

					if(this->GetHttpContentLength()<=0)
					{
						if((data[i-1]==13)&&(data[i] == 10)&&(!m_foundLen))
						{
							

							if((m_endContent)&&(!m_beginContent))
							{
								hexContentLen = tmpData.substr(0,i).c_str();
								SetHexContentLen(hexContentLen);
								m_beginContent = true;
								m_endContent = false;

								if(this->m_debugMode)
								{
									printf("begin contentLen\n");
								}
							}
							else if((m_beginContent)&&(!m_endContent))
							{
								m_endContent = true;
								m_beginContent = false;
								
								if(this->m_debugMode)
								{
									printf("end contentLen\n");
								}
							}
							else if((!m_beginContent)&&(!m_endContent))
							{
								hexContentLen = tmpData.substr(0,i).c_str();
								SetHexContentLen(hexContentLen);
								m_beginContent = true;

								if(this->m_debugMode)
								{
									printf("start contentLen\n");
									printf("%s\n",tmpData.substr(0,i).c_str());
								}
							}
							
							
							if(this->m_debugMode)
							{
								//printf("hex len set to:%s\n",hexContentLen.c_str());
								printf("inside contentLen\n");
								
							}
							
							
							tmpData = "";

							if(this->GetHttpContentLength()>0)
							{
								// Reset responseData
								m_foundLen = true;
								justSetChunkedContentlen = true;

								if(this->m_debugMode)
								{
									printf("found contentLen\n");
								}
							}

						}
					}
					
					//printf("chunk with count:%d\n",chunkCount);
						
					

					if(m_foundLen)
					{
						if((this->m_httpResponseDataCount) ==this->GetHttpContentLength())
						{
							if(this->m_debugMode)
							{
								printf("found the end of a chunk\n");
							}

							this->SetContentLength(0);
							this->m_httpResponseDataCount = 0;
							m_foundLen = false;
						}
					}

					// remove CR
					if(justSetChunkedContentlen)
					{
						//this->m_responseData = this->m_responseData.substr(0,m_responseData.length()-1).c_str();
						justSetChunkedContentlen = false;
					}
					else
					{
						if((this->GetHttpContentLength()>0))
						{
							this->m_responseData += data[i];
							this->m_httpResponseDataCount++;
						}
					}
				}

				if(this->GetHttpContentLength()==this->m_httpResponseDataCount)
				{
					if(this->m_debugMode)
					{
						printf("data count whe done:%d!\n",this->m_httpResponseDataCount);
					}
					
					this->m_foundEndHtml = true;
					m_foundLen = false;
					SetCloseAndDelete();
				}
			}
			else
			{
				if(this->m_debugMode)
				{
					printf("redirect location Set!\n");
				}

				this->SetCloseAndDelete();
			}
		}

		if(this->m_debugMode)
		{
			printf("ContentLen:%d\n",GetHttpContentLength());
			printf("m_responseLen total is %d\n", this->m_httpResponseDataCount);
		}
	}

	void OnHeader(const std::string& key,const std::string& value)
	{
		if(this->m_debugMode)
		{
			printf("OnResponseHeader(): %s: %s\n",key.c_str(),value.c_str());
		}

		if (!strcasecmp(key.c_str(),"content-encoding"))
		{
			m_contentEncoding = value;
			this->m_contentEncoded = true;

			if(strcmp(value.c_str(), "gzip")==0)
			{
				m_gzipEncoded = true;	
			}
		}

		if (!strcasecmp(key.c_str(),"transfer-encoding"))
		{
			m_transferEncoding = value;
			this->m_transferEndoded = true;
		}

		if (!strcasecmp(key.c_str(),"p3p"))
		{
			m_p3pSetting = value;
			this->m_p3pSet = true;
		}

		if (!strcasecmp(key.c_str(),"set-cookie"))
		{
			
			if(GetCookie().find(value)==std::string::npos)
			{
				
				m_cookieSetting+= value;
				m_cookieSetting+=";";

				//this->SetCookie(m_cookieSetting);
				this->m_cookieSet = true;
			}
		}

		if (!strcasecmp(key.c_str(),"cache-control"))
		{
			m_cacheControl = value;
			this->m_cacheControlSet = true;
		}

		if (!strcasecmp(key.c_str(),"vary"))
		{
			m_varyValue = value;
			this->m_varySet = true;
		}

		if (!strcasecmp(key.c_str(),"connection"))
		{
			this->SetConnection(value);
		}

		if (!strcasecmp(key.c_str(),"content-type"))
		{
			m_contentType = value;
		}

		if (!strcasecmp(key.c_str(),"content-length"))
		{
			this->SetContentLength(atoi(value.c_str()));
		}

		if(!strcasecmp(key.c_str(),"http/1.1"))
		{
			this->m_httpStatusHeader = value;
		}

		if(!strcasecmp(key.c_str(),"location")&&(!m_redirectLocationFound))
		{
			this->m_httpRedirectLocation = value;
			m_redirectLocationSet = true;

			if(this->m_debugMode)
			{
				printf("just set redirectLocation to true:%s\n",value.c_str());
			}
		}

		if (!strcasecmp(key.c_str(),"referer"))
		{
			SetHttpReferer(value);
		}
	}

	void OnConnect()
	{
		char buffer [33];

		if(this->m_debugMode)
		{
			printf("proxy-onConnect\n");
		}

		if (GetUrlPort() != 80 && GetUrlPort() != 443)
			AddResponseHeader( "Host", GetUrlHost() + " " + Utility::l2string(GetUrlPort()) );
		else
			AddResponseHeader( "Host", GetUrlHost() );

		if(this->m_debugMode)
		{
			printf("proxyClientHost:%s\n",this->GetUrlHost().c_str());
		}
		AddResponseHeader( "User-agent", MyUseragent() );		
		AddResponseHeader( "Accept", "text/xml,application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,video/x-mng,image/png,image/jpeg,image/gif;q=0.2,*/*;q=0.1");
		AddResponseHeader( "Accept-Language", "en-us,en;q=0.5");
		AddResponseHeader( "Accept-Charset", "ISO-8859-1,utf-8;q=0.7,*;q=0.7");
		AddResponseHeader( "Keep-Alive", std::string(itoa(this->GetKeepAlive(),buffer,10)));
		AddResponseHeader( "Connection", this->GetConnection() );
		AddResponseHeader( "Referer", this->GetHttpReferer());
		if(this->m_debugMode)
		{
			printf("proxyClientReferrer:%s\n",this->GetHttpReferer().c_str());
		}
		AddResponseHeader( "Cookie", this->GetCookie());

		if(this->m_debugMode)
		{
			printf("proxyClientCookie:%s\n",this->GetCookie().c_str());
		}

		if(GetMethod() == "POST")
		{
			m_hadPost = true;
			AddResponseHeader( "Content-Type", GetHttpContentType() );
			AddResponseHeader( "Content-Length", std::string(itoa(Gemensamt::GetHttpContentLength(),buffer,10)));
			
			SendRequest();
			DEB(
				printf("Sent POST request\n");
			)
			
			
			// send body
			Send( m_postData );
			DEB(
				printf("Sent POST Data\n");
			)
		}
		else
		{
			AddResponseHeader( "Cache-Control", "no-cache");

			SendRequest();
		}

		m_requestHeader = GetRequest();
		//printf("requestHeader:%s\n",GetRequest().c_str());
	}

	/** Add field to post. */
	void AddField(const std::string& name,const std::string& value)
	{ 
		std::list<std::string> vec;
		vec.push_back(value);
		AddMultilineField(name, vec);
	}

	void AddMultilineField(const std::string& name,std::list<std::string>& values)
	{
		m_fields[name] = values;
	}

	void AddPostData(std::string value)
	{
		m_postData = value;
	}

	bool HadPost()
	{
		return m_hadPost;
	}

private:
		std::map<std::string,std::list<std::string> > m_fields;
		std::string m_postData;
		std::string m_responseData;
		std::string m_responseHeader;
		std::string m_requestHeader;
		std::string m_responseStatus;
		std::string m_responseStatusText;
		std::string m_contentEncoding;
		std::string m_transferEncoding;
		std::string m_p3pSetting;
		std::string m_cookieSetting;
		std::string m_cookieSession;
		std::string m_cacheControl;
		std::string m_varyValue;
		std::string m_connection;
		std::string m_contentType;
		std::string m_proxyLine;
		std::string m_httpStatusHeader;
		std::string m_httpRedirectLocation;
		std::string m_destinationIP;
		int m_httpResponseDataCount;
		bool m_gzipEncoded;
		bool m_transferEndoded;
		bool m_p3pSet;
		bool m_cookieSet;
		bool m_hostCookieSet;
		bool m_cacheControlSet;
		bool m_varySet;
		bool m_contentEncoded;
		bool m_proxyHeader;
		bool m_redirectLocationSet;
		bool m_redirectLocationFound;
		bool m_continueSet;
		bool m_foundEndHtml;
		bool m_foundLen;
		bool m_beginContent;
		bool m_endContent;
		bool m_hadPost;
		double m_responseTime;
};

#ifdef SOCKETS_NAMESPACE
}
#endif

#endif // _PROXYHTTPGETSOCKET_H
