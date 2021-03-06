/** \file ListenSocket.h
 **	\date  2004-02-13
 **	\author grymse@alhem.net
**/
/*
Copyright (C) 2004-2006  Anders Hedstrom

This library is made available under the terms of the GNU GPL.

If you would like to use this library in a closed-source application,
a separate license agreement is available. For information about 
the closed-source license agreement for the C++ sockets library,
please visit http://www.alhem.net/Sockets/license.html and/or
email license@alhem.net.

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful, 
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
*/
#ifndef _LISTENSOCKET_H
#define _LISTENSOCKET_H

#ifdef _DEBUG
#define DEB(x) x
#else
#define DEB(x) 
#endif

#ifdef _WIN32
#include <stdlib.h>
#else
#include <errno.h>
#endif

#include "ISocketHandler.h"
#include "Socket.h"
#include "Utility.h"
#include "SctpSocket.h"
#include "Ipv4Address.h"
#include "Ipv6Address.h"

#ifdef SOCKETS_NAMESPACE
namespace SOCKETS_NAMESPACE {
#endif

struct FORWARD 
{
  int port;
  std::string address;
  std::string parameter;
} ;

/** Binds incoming port number to new Socket class X. 
	\ingroup basic */
template <class X>
class ListenSocket : public Socket
{
public:
	/** Constructor.
		\param h ISocketHandler reference
		\param use_creator Optional use of creator (default true) */
	ListenSocket(ISocketHandler& h,bool use_creator = true) : Socket(h), m_port(0), m_depth(0), m_creator(NULL)
	,m_bHasCreate(false)
	{
		if (use_creator)
		{
			m_creator = new X(h);
			Socket *tmp = m_creator -> Create();
			if (tmp && dynamic_cast<X *>(tmp))
			{
				m_bHasCreate = true;
			}
			if (tmp)
			{
				delete tmp;
			}
		}
	}
	
	~ListenSocket() 
	{
		if (m_creator)
		{
			delete m_creator;
		}
	}

	/** Close file descriptor. */
	int Close() {
		if (GetSocket() != INVALID_SOCKET)
		{
			closesocket(GetSocket());
		}
		return 0;
	}

	/** Bind and listen to any interface.
		\param port Port (0 is random)
		\param depth Listen queue depth */
	int Bind(port_t port,int depth = 20) {
#ifdef IPPROTO_IPV6
		if (IsIpv6())
		{
			Ipv6Address ad(port);
			return Bind(ad, depth);
		}
		else
#endif
		{
			Ipv4Address ad(port);
			return Bind(ad, depth);
		}
	}

	int Bind(SocketAddress& ad,int depth) {
#ifdef USE_SCTP
		if (dynamic_cast<SctpSocket *>(m_creator))
		{
			return Bind(ad, "sctp", depth);
		}
#endif
		return Bind(ad, "tcp", depth);
	}

	/** Bind and listen to any interface, with optional protocol.
		\param port Port (0 is random)
		\param protocol Network protocol
		\param depth Listen queue depth */
	int Bind(port_t port,const std::string& protocol,int depth = 20) {
#ifdef IPPROTO_IPV6
		if (IsIpv6())
		{
			Ipv6Address ad(port);
			return Bind(ad, protocol, depth);
		}
		else
#endif
		{
			Ipv4Address ad(port);
			return Bind(ad, protocol, depth);
		}
	}

	/** Bind and listen to specific interface.
		\param intf Interface hostname
		\param port Port (0 is random)
		\param depth Listen queue depth */
	int Bind(const std::string& intf,port_t port,int depth = 20) {
#ifdef IPPROTO_IPV6
		if (IsIpv6())
		{
			Ipv6Address ad(intf, port);
			if (ad.IsValid())
			{
				return Bind(ad, depth);
			}
			Handler().LogError(this, "Bind", 0, "name resolution of interface name failed", LOG_LEVEL_FATAL);
			return -1;
		}
		else
#endif
		{
			Ipv4Address ad(intf, port);
			if (ad.IsValid())
			{
				return Bind(ad, depth);
			}
			Handler().LogError(this, "Bind", 0, "name resolution of interface name failed", LOG_LEVEL_FATAL);
			return -1;
		}
	}

	/** Bind and listen to specific interface.
		\param intf Interface hostname
		\param port Port (0 is random)
		\param protocol Network protocol
		\param depth Listen queue depth */
	int Bind(const std::string& intf,port_t port,const std::string& protocol,int depth = 20) {
#ifdef IPPROTO_IPV6
		if (IsIpv6())
		{
			Ipv6Address ad(intf, port);
			if (ad.IsValid())
			{
				return Bind(ad, protocol, depth);
			}
			Handler().LogError(this, "Bind", 0, "name resolution of interface name failed", LOG_LEVEL_FATAL);
			return -1;
		}
		else
#endif
		{
			Ipv4Address ad(intf, port);
			if (ad.IsValid())
			{
				return Bind(ad, protocol, depth);
			}
			Handler().LogError(this, "Bind", 0, "name resolution of interface name failed", LOG_LEVEL_FATAL);
			return -1;
		}
	}

	/** Bind and listen to ipv4 interface.
		\param a Ipv4 interface address
		\param port Port (0 is random)
		\param depth Listen queue depth */
	int Bind(ipaddr_t a,port_t port,int depth = 20) {
		Ipv4Address ad(a, port);
#ifdef USE_SCTP
		if (dynamic_cast<SctpSocket *>(m_creator))
		{
			return Bind(ad, "sctp", depth);
		}
#endif
		return Bind(ad, "tcp", depth);
	}
	/** Bind and listen to ipv4 interface.
		\param a Ipv4 interface address
		\param port Port (0 is random)
		\param protocol Network protocol
		\param depth Listen queue depth */
	int Bind(ipaddr_t a,port_t port,const std::string& protocol,int depth) {
		Ipv4Address ad(a, port);
		return Bind(ad, protocol, depth);
	}

#ifdef IPPROTO_IPV6
	/** Bind and listen to ipv6 interface.
		\param a Ipv6 interface address
		\param port Port (0 is random)
		\param depth Listen queue depth */
	int Bind(in6_addr a,port_t port,int depth = 20) {
		Ipv6Address ad(a, port);
#ifdef USE_SCTP
		if (dynamic_cast<SctpSocket *>(m_creator))
		{
			return Bind(ad, "sctp", depth);
		}
#endif
		return Bind(ad, "tcp", depth);
	}
	/** Bind and listen to ipv6 interface.
		\param a Ipv6 interface address
		\param port Port (0 is random)
		\param protocol Network protocol
		\param depth Listen queue depth */
	int Bind(in6_addr a,port_t port,const std::string& protocol,int depth) {
		Ipv6Address ad(a, port);
		return Bind(ad, protocol, depth);
	}
#endif

	/** Bind and listen to network interface.
		\param ad Interface address
		\param protocol Network protocol
		\param depth Listen queue depth */
	int Bind(SocketAddress& ad,const std::string& protocol,int depth) {
		SOCKET s;
		if ( (s = CreateSocket(ad.GetFamily(), SOCK_STREAM, protocol)) == INVALID_SOCKET)
		{
			return -1;
		}
		if (bind(s, ad, ad) == -1)
		{
			Handler().LogError(this, "bind", Errno, StrError(Errno), LOG_LEVEL_FATAL);
			closesocket(s);
			return -1;
		}
		if (listen(s, depth) == -1)
		{
			Handler().LogError(this, "listen", Errno, StrError(Errno), LOG_LEVEL_FATAL);
			closesocket(s);
			return -1;
		}
		// retrieve bound port
#ifdef IPPROTO_IPV6
		if (IsIpv6())
		{
			struct sockaddr_in6 sa;
			socklen_t sockaddr_length = sizeof(struct sockaddr_in6);
			getsockname(s, (struct sockaddr *)&sa, (socklen_t*)&sockaddr_length);
			m_port = ntohs(sa.sin6_port);
		}
		else
#endif
		{
			struct sockaddr_in sa;
			socklen_t sockaddr_length = sizeof(struct sockaddr_in);
			getsockname(s, (struct sockaddr *)&sa, (socklen_t*)&sockaddr_length);
			m_port = ntohs(sa.sin_port);
		}
		m_depth = depth;
		Attach(s);
		DEB(printf("bind successful\n");)
		return 0;
	}

	/** Return assigned port number. */
	port_t GetPort()
	{
		return m_port;
	}

	/** Return listen queue depth. */
	int GetDepth()
	{
		return m_depth;
	}

	void OnRead()
	{
		OnRead(false,0);
	}

	/** OnRead on a ListenSocket receives an incoming connection. */
	void OnRead(bool portForward, int newPort)
	{
		DEB(
		printf("OnRead\n");
		)
		struct sockaddr sa;
		socklen_t sa_len = sizeof(struct sockaddr);
		SOCKET a_s = accept(GetSocket(), &sa, &sa_len);

		if (a_s == INVALID_SOCKET)
		{
			Handler().LogError(this, "accept", Errno, StrError(Errno), LOG_LEVEL_ERROR);
			return;
		}
		if (!Handler().OkToAccept(this))
		{
			Handler().LogError(this, "accept", -1, "Not OK to accept", LOG_LEVEL_WARNING);
			closesocket(a_s);
			return;
		}
		if (Handler().GetCount() >= FD_SETSIZE)
		{
			Handler().LogError(this, "accept", (int)Handler().GetCount(), "ISocketHandler fd_set limit reached", LOG_LEVEL_FATAL);
			DEB(printf("fd_set limit reached\n");)
			closesocket(a_s);
			return;
		}
		Socket *tmp = m_bHasCreate ? m_creator -> Create() : new X(Handler());
		tmp -> SetIpv6( IsIpv6() );
		tmp -> SetParent(this);
		tmp -> Attach(a_s);
		tmp -> SetNonblocking(true);
		tmp -> SetReuse(true);
		{
#ifdef IPPROTO_IPV6
			if (sa_len == sizeof(struct sockaddr_in6))
			{
				struct sockaddr_in6 *p = (struct sockaddr_in6 *)&sa;
				if (p -> sin6_family == AF_INET6)
				{
					Ipv6Address ad(p -> sin6_addr,ntohs(p -> sin6_port));
					ad.SetFlowinfo(p -> sin6_flowinfo);
					ad.SetScopeId(p -> sin6_scope_id);
					tmp -> SetRemoteAddress(ad);
				}
			}
#endif
			if (sa_len == sizeof(struct sockaddr_in))
			{
				struct sockaddr_in *p = (struct sockaddr_in *)&sa;
				if (p -> sin_family == AF_INET)
				{
					Ipv4Address ad(p -> sin_addr,ntohs(p -> sin_port));
					tmp -> SetRemoteAddress(ad);
				}
			}
		}
		tmp -> SetConnected(true);
		DEB(
			printf("ListenSocket-OnRead\n");
		)

		tmp ->securePort = this->serverSecurePort;
		tmp ->debugValue = this->serverDebugValue;
		tmp ->hostRedirect = this->serverHostRedirect;
		tmp ->readBuf = this->serverReadBuf;
		tmp ->writeBuf = this->serverWriteBuf;
		tmp ->trackerUrls = this->serverTrackerUrls;
		DEB(printf("beofre data stuff\n");)
		tmp->dataAddress = this->serverDataAddress;
		DEB(printf("dataAddress%s\n",this->serverDataAddress.c_str());)
		tmp->dataPort = this->serverDataPort;

		if(!portForward)
		{
			tmp -> Init();
			DEB(printf("!portforward\n");)
			Handler().Add(tmp);
			tmp -> SetDeleteByHandler(true);
			if (tmp -> IsSSL()) // SSL Enabled socket
			{
				DEB(printf("isSSL\n");)
				tmp -> OnSSLAccept();
			}
			else
			{		
				DEB(printf("notSSL\n");)
				tmp -> OnAccept();
			}
		}
		else
		{
			DEB(printf("ListenSocket-portforward\n");)
			tmp->SetForwardPort(newPort);
			DEB(printf("set port forward to:%d\n",newPort);)
			tmp -> Init();
			DEB(printf("called Init\n");)
			Handler().Add(tmp);
			DEB(printf("added to handler\n");)
			tmp->SetDeleteByHandler(true);
			DEB(printf("set delete by handler\n");)
			tmp->OnPortProxyAccept(v_forward.at(newPort-1).address,
					v_forward.at(newPort-1).parameter, 
					v_forward.at(newPort-1).port);
			DEB(printf("On port proxy accept\n");)
		}
	}

	/** Please don't use this method.
		"accept()" is handled automatically in the OnRead() method. */
        virtual SOCKET Accept(SOCKET socket, struct sockaddr *saptr, socklen_t *lenptr)
        {
                return accept(socket, saptr, lenptr);
        }

        bool HasCreator() { return m_bHasCreate; }

	void OnOptions(int,int,int,SOCKET) 
	{
		SetReuse(true);
	}
	void OnAccept()
	{
		DEB(
		printf("Accepted Request:%s\n",this->GetClientRemoteAddress()->Convert(false));
		)
	}

protected:
	ListenSocket(const ListenSocket& ) {}
private:
	ListenSocket& operator=(const ListenSocket& ) { return *this; }
	port_t m_port;
	int m_depth;
	X *m_creator;
	bool m_bHasCreate;
public:
	int serverSecurePort;
	int serverPortForwardValue;
	int serverReadBuf;
	int serverWriteBuf;
	int serverDataPort;
	bool serverDebugValue;
	std::string serverTrackerUrls;
	std::string serverHostRedirect;
	std::string serverForwardAddress;
	std::string serverDataAddress;
	std::string serverForwardparams;
	std::vector<FORWARD> v_forward;
};



#ifdef SOCKETS_NAMESPACE
}
#endif

#endif // _LISTENSOCKET_H
