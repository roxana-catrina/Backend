package Licenta.Licenta.Repository;

import Licenta.Licenta.Model.Tara;
import org.hibernate.sql.ast.tree.expression.JdbcParameter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaraRepository extends JpaRepository<Tara,Long> {
Tara findByPrefix(String prefix);

}
