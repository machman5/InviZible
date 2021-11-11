/*
    This file is part of InviZible Pro.

    InviZible Pro is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    InviZible Pro is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with InviZible Pro.  If not, see <http://www.gnu.org/licenses/>.

    Copyright 2019-2021 by Garmatin Oleksandr invizible.soft@gmail.com
 */

package pan.alexander.tordnscrypt.data.dns_resolver

import pan.alexander.tordnscrypt.utils.dns.Record
import pan.alexander.tordnscrypt.domain.dns_resolver.DnsRepository
import javax.inject.Inject

class DnsRepositoryImpl @Inject constructor(
    private val dnsDataSource: DnsDataSource
) : DnsRepository {

    override fun resolveDomainUDP(domain: String, port: Int): Set<String> {
        return dnsDataSource.resolveDomainUDP(domain, port)
            ?.filter { isRecordValid(it) }
            ?.flatMap {
                when {
                    it.isA || it.isAAAA -> listOf(it.value.trim())
                    it.isCname -> resolveDomainUDP("https://${it.value}", port)
                    else -> emptyList()
                }
            }
            ?.toHashSet() ?: emptySet()
    }

    override fun resolveDomainDOH(domain: String): Set<String> {
        return dnsDataSource.resolveDomainDOH(domain)
            ?.filter { isRecordValid(it) }
            ?.flatMap {
                when {
                    it.isA || it.isAAAA -> listOf(it.value.trim())
                    it.isCname -> resolveDomainDOH("https://${it.value}")
                    else -> emptyList()
                }
            }
            ?.toHashSet() ?: emptySet()
    }

    override fun reverseResolve(ip: String): String {
        return dnsDataSource.reverseResolve(ip)
    }

    private fun isRecordValid(record: Record?): Boolean {
        return record?.value != null && record.value.isNotEmpty() && !record.isExpired
    }
}