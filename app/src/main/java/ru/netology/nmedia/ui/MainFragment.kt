package ru.netology.nmedia.ui

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.FragmentMainBinding
import ru.netology.nmedia.ui.decorators.*
import java.time.LocalDateTime

class MainFragment : Fragment() {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            rcViewPost.layoutManager =
                LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
            rcViewPost.adapter = PostAdapter(fillPosts())
            rcViewPost.addItemDecoration(
                LinearVerticalSpacingDecoration(
                    AndroidUtils.dpToPx(activity as AppCompatActivity, 5)
                )
            )
        }

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun fillPosts(): List<Post> = listOf(
        Post(
            1L,
            "Университет Нетологияяяяяяяяяяяяяяяяяяяяяяяяяяяяяяяяяяяяя",
            "fashjkgafhsjkfhasjkfhasjkfasjkfasljkfasjklfajklsjklfasljkfasjklffajkljkfls" +
                    "fasjasfhjkfhasjkfhasjkfhasjkfahsjkfhajkshjfaskfhasjkhjkasfhjkfashjkfashjfaks" +
                    "fasljkafsljkflasjkfasjklfalsjkflajksfajlskaflsjk" +
                    "fasjkfasljkflasjkflasjkfljkasljkffaslj;fasjlkfasjklfasjklas" +
                    "faslkfasklfasljkfasljkfasljfasljkfasljkfasljkflasjkk" +
                    "fsalkjfasljkfasjlkflasjkflajksflasjkflasjkflasjkljkfasljkfasflasjk",
            R.mipmap.ic_launcher,
            LocalDateTime.now(),
            likes = 1000,
            views = 1100
        ),
        Post(
            2L,
            "Университет Нетология",
            "fashjkgafhsjkfhasjkfhasjkfasjkfasljkfasjklfajklsjklfasljkfasjklffajkljkfls" +
                    "fasjasfhjkfhasjkfhasjkfhasjkfahsjkfhajkshjfaskfhasjkhjkasfhjkfashjkfashjfaks" +
                    "fasljkafsljkflasjkfasjklfalsjkflajksfajlskaflsjk" +
                    "fasjkfasljkflasjkflasjkfljkasljkffaslj;fasjlkfasjklfasjklas" +
                    "faslkfasklfasljkfasljkfasljfasljkfasljkfasljkflasjkk" +
                    "fsalkjfasljkfasjlkflasjkflajksflasjkflasjkflasjkljkfasljkfasflasjk",
            R.mipmap.ic_launcher,
            LocalDateTime.now()
        ),
        Post(
            3L,
            "Университет Нетология",
            "fashjkgafhsjkfhasjkfhasjkfasjkfasljkfasjklfajklsjklfasljkfasjklffajkljkfls" +
                    "fasjasfhjkfhasjkfhasjkfhasjkfahsjkfhajkshjfaskfhasjkhjkasfhjkfashjkfashjfaks" +
                    "fasljkafsljkflasjkfasjklfalsjkflajksfajlskaflsjk" +
                    "fasjkfasljkflasjkflasjkfljkasljkffaslj;fasjlkfasjklfasjklas" +
                    "faslkfasklfasljkfasljkfasljfasljkfasljkfasljkflasjkk" +
                    "fsalkjfasljkfasjlkflasjkflajksflasjkflasjkflasjkljkfasljkfasflasjk",
            R.mipmap.ic_launcher,
            LocalDateTime.now(),
            views = 100000000
        ),
        Post(
            4L,
            "Университет Нетология",
            "fashjkgafhsjkfhasjkfhasjkfasjkfasljkfasjklfajklsjklfasljkfasjklffajkljkfls" +
                    "fasjasfhjkfhasjkfhasjkfhasjkfahsjkfhajkshjfaskfhasjkhjkasfhjkfashjkfashjfaks" +
                    "fasljkafsljkflasjkfasjklfalsjkflajksfajlskaflsjk" +
                    "fasjkfasljkflasjkflasjkfljkasljkffaslj;fasjlkfasjklfasjklas" +
                    "faslkfasklfasljkfasljkfasljfasljkfasljkfasljkflasjkk" +
                    "fsalkjfasljkfasjlkflasjkflajksflasjkflasjkflasjkljkfasljkfasflasjk",
            R.mipmap.ic_launcher,
            LocalDateTime.now(),
            comments = 100000,
            likes = 1000000,
            shared = 101001
        ),
        Post(
            5L,
            "Университет Нетология",
            "fashjkgafhsjkfhasjkfhasjkfasjkfasljkfasjklfajklsjklfasljkfasjklffajkljkfls" +
                    "fasjasfhjkfhasjkfhasjkfhasjkfahsjkfhajkshjfaskfhasjkhjkasfhjkfashjkfashjfaks" +
                    "fasljkafsljkflasjkfasjklfalsjkflajksfajlskaflsjk" +
                    "fasjkfasljkflasjkflasjkfljkasljkffaslj;fasjlkfasjklfasjklas" +
                    "faslkfasklfasljkfasljkfasljfasljkfasljkfasljkflasjkk" +
                    "fsalkjfasljkfasjlkflasjkflajksflasjkflasjkflasjkljkfasljkfasflasjk",
            R.mipmap.ic_launcher,
            LocalDateTime.now(),
            comments = 12_100,
            likes = 12_100,
            shared = 12_100,
            views = 12_100
        ),
        Post(
            6L,
            "Университет Нетология",
            "fashjkgafhsjkfhasjkfhasjkfasjkfasljkfasjklfajklsjklfasljkfasjklffajkljkfls" +
                    "fasjasfhjkfhasjkfhasjkfhasjkfahsjkfhajkshjfaskfhasjkhjkasfhjkfashjkfashjfaks" +
                    "fasljkafsljkflasjkfasjklfalsjkflajksfajlskaflsjk" +
                    "fasjkfasljkflasjkflasjkfljkasljkffaslj;fasjlkfasjklfasjklas" +
                    "faslkfasklfasljkfasljkfasljfasljkfasljkfasljkflasjkk" +
                    "fsalkjfasljkfasjlkflasjkflajksflasjkflasjkflasjkljkfasljkfasflasjk",
            R.mipmap.ic_launcher,
            LocalDateTime.now(),
            views = 1_100_000
        ),
        Post(
            7L,
            "Университет Нетология",
            "fashjkgafhsjkfhasjkfhasjkfasjkfasljkfasjklfajklsjklfasljkfasjklffajkljkfls" +
                    "fasjasfhjkfhasjkfhasjkfhasjkfahsjkfhajkshjfaskfhasjkhjkasfhjkfashjkfashjfaks" +
                    "fasljkafsljkflasjkfasjklfalsjkflajksfajlskaflsjk" +
                    "fasjkfasljkflasjkflasjkfljkasljkffaslj;fasjlkfasjklfasjklas" +
                    "faslkfasklfasljkfasljkfasljfasljkfasljkfasljkflasjkk" +
                    "fsalkjfasljkfasjlkflasjkflajksflasjkflasjkflasjkljkfasljkfasflasjk",
            R.mipmap.ic_launcher,
            LocalDateTime.now()
        ),
    )


    companion object {
        @JvmStatic
        fun newInstance() = MainFragment()
    }
}