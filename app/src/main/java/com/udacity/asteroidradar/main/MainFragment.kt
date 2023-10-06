package com.udacity.asteroidradar.main

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.udacity.asteroidradar.Asteroid
import com.udacity.asteroidradar.R
import com.udacity.asteroidradar.databinding.FragmentMainBinding

class MainFragment : Fragment() {

    private val viewModel: MainViewModel by lazy {
        ViewModelProvider(this)[MainViewModel::class.java]
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val binding = FragmentMainBinding.inflate(inflater)
        binding.lifecycleOwner = this

        binding.viewModel = viewModel

        val adapter = AsteroidAdapter(AsteroidListener { asteroidId ->
            viewModel.onAsteroidSelect(asteroidId)
        })

        binding.asteroidRecycler.adapter = adapter

        /*
        Here I will attempt to submit a dummy list to test my recycler view
         */
        fun generateDummyAsteroidList(): List<Asteroid> {
            val dummyAsteroidList = mutableListOf<Asteroid>()

            for (i in 1..15) {
                val asteroid = Asteroid(
                    id = i.toLong(),
                    codename = "Asteroid $i",
                    closeApproachDate = "2023-10-0$i",
                    absoluteMagnitude = 5.0 + i.toDouble(),
                    estimatedDiameter = 100.0 + i.toDouble(),
                    relativeVelocity = 200.0 + i.toDouble(),
                    distanceFromEarth = 100000.0 + i.toDouble(),
                    isPotentiallyHazardous = i % 2 == 0
                )
                dummyAsteroidList.add(asteroid)
            }

            return dummyAsteroidList
        }

        adapter.submitList(generateDummyAsteroidList())

        setHasOptionsMenu(true)

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main_overflow_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return true
    }
}
