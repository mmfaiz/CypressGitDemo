import org.grails.plugin.hibernate.filter.DefaultHibernateFiltersHolder
import org.hibernate.HibernateException
import org.hibernate.Session

class HibernateFilterFilters {

	def sessionFactory

	def filters = {
		enableHibernateFilters(controller:'*', action:'*') {
			before = {
				Session session
				try {
					session = sessionFactory.currentSession
				} catch (HibernateException ex) {
					log.debug("No Hibernate session found for HibernateFilterInterceptor", ex)
					session = null
				}
				if (session) {
					for (String name in DefaultHibernateFiltersHolder.defaultFilters) {
						session.enableFilter name
					}
				}
				true
			}
		}
	}
}
