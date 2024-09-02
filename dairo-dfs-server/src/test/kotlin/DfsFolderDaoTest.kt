import cn.dairo.dfs.DairoApplication
import cn.dairo.dfs.service.DfsFileService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(classes = [DairoApplication::class])
class DfsFolderDaoTest {


    @Autowired
    private lateinit var dfsFolderService: DfsFileService

    @Test
    fun getIdByPath() {
    }

    @Test
    fun mkdirs() {
    }
}